# 接入 Kafka 层实现文档（一期 / 审计链路 MVP）

## 1. 文档目标

本文档用于指导后端在 **不改变现有接口语义** 的前提下，将审计日志异步链路从本地线程池改造为 Kafka 事件流。

目标范围：
- 仅接入审计链路（AuditLog）
- 保留原有事务后触发语义（afterCommit）
- 提供失败重试、死信队列（DLT）与可降级开关

非目标范围：
- 不改造登录/鉴权主链路
- 不引入分布式事务
- 不在一期实现完整 Outbox Pattern

---

## 2. 当前现状与改造原则

### 2.1 当前现状（基于现有代码）

- 审计入口：`AuditLogServiceImpl#record`
- 异步落库：`AuditLogPersistServiceImpl#persist`（`@Async("auditLogExecutor")`）
- 线程池配置：`AuditAsyncConfig` + `AuditAsyncProperties`

### 2.2 改造原则

1. Controller/Service 的业务接口不变。
2. 事务提交后再发送消息（保证“提交成功才记录成功审计”）。
3. 消费端落库失败可重试，超过阈值进入 DLT。
4. 保留降级能力：Kafka 异常时可回退本地异步落库。
5. 全链路可观测：日志中包含 `traceId/requestId/eventId`。

---

## 3. 架构设计

### 3.1 逻辑流程

1. 请求进入业务接口，执行原有逻辑。
2. 业务触发 `AuditLogService.record(request)`。
3. 若存在事务：
   - 成功审计：`afterCommit` 发送 Kafka 消息。
   - 失败审计：`afterCompletion != COMMITTED` 时发送 Kafka 消息。
4. Kafka Consumer 消费 `AuditLogEvent`，执行审计落库。
5. 消费失败进入重试；重试耗尽后发送至 DLT。

### 3.2 组件分层建议

建议新增包结构：

```text
com.chordsked.backend.kafka
├── config
│   ├── KafkaProducerConfig.java
│   ├── KafkaConsumerConfig.java
│   └── KafkaErrorHandlerConfig.java
├── model
│   └── AuditLogEvent.java
├── producer
│   └── AuditLogEventPublisher.java
└── consumer
    └── AuditLogEventConsumer.java
```

说明：
- `service` 仅依赖 `producer` 接口，不直接依赖底层 Kafka 细节。
- `consumer` 专注“反序列化 + 校验 + 调用 DAO 落库”。

---

## 4. 配置设计

## 4.1 Maven 依赖

在 `backend/pom.xml` 增加：

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## 4.2 application 配置

### 4.2.1 公共配置（`common/application-common.yml`）

```yaml
chordsked:
  kafka:
    audit:
      enabled: ${CHORDSKED_KAFKA_AUDIT_ENABLED:false}
      topic: ${CHORDSKED_KAFKA_AUDIT_TOPIC:chordsked.audit.log.v1}
      group-id: ${CHORDSKED_KAFKA_AUDIT_GROUP_ID:chordsked-audit-log-group}
      concurrency: ${CHORDSKED_KAFKA_AUDIT_CONCURRENCY:1}
      max-retries: ${CHORDSKED_KAFKA_AUDIT_MAX_RETRIES:3}
      backoff-millis: ${CHORDSKED_KAFKA_AUDIT_BACKOFF_MILLIS:2000}
      dlt-suffix: ${CHORDSKED_KAFKA_AUDIT_DLT_SUFFIX:.DLT}
```

### 4.2.2 环境配置（`env/*/application-*.yml`）

```yaml
spring:
  kafka:
    bootstrap-servers: ${CHORDSKED_KAFKA_BOOTSTRAP_SERVERS:127.0.0.1:9092}
    producer:
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
    consumer:
      enable-auto-commit: false
      auto-offset-reset: latest
      properties:
        spring.json.trusted.packages: com.chordsked.backend.kafka.model
```

说明：
- `bootstrap-servers` 必须按环境覆盖，禁止硬编码线上地址。
- 一期默认 dev 开启，其他环境先关闭（`enabled=false`）。

---

## 5. 关键类设计

## 5.1 配置属性类

新增 `KafkaAuditProperties`（建议 `@ConfigurationProperties(prefix = "chordsked.kafka.audit")`）。

建议字段：
- `enabled`
- `topic`
- `groupId`
- `concurrency`
- `maxRetries`
- `backoffMillis`
- `dltSuffix`

约束建议：
- `topic/groupId/dltSuffix`：`@NotBlank`
- 数值字段：`@Min(1)`（或合理最小值）

## 5.2 事件对象

`AuditLogEvent` 建议字段：
- `eventId`（UUID，幂等键）
- `eventVersion`（当前固定 `v1`）
- `occurredAt`（毫秒时间戳）
- `traceId`
- `requestId`
- 审计业务字段（moduleName/actionType/bizId/...）

要求：
- 字段语义稳定，避免频繁变更。
- 新增字段保持向后兼容（消费者忽略未知字段）。

## 5.3 生产者

`AuditLogEventPublisher` 职责：
- 入参校验（`event`、`topic`）
- 使用 `KafkaTemplate<String, AuditLogEvent>` 发送
- key 建议使用 `eventId`（方便排查）
- 发送失败记录 error 日志并抛出/降级（按开关策略）

## 5.4 消费者

`AuditLogEventConsumer` 职责：
- `@KafkaListener(topics = "...", groupId = "...")`
- 反序列化后做关键字段校验
- 调用 `AuditLogDao` 落库
- 成功后手动 ack
- 失败抛异常交给 `DefaultErrorHandler`

## 5.5 错误处理与 DLT

使用：
- `DefaultErrorHandler`
- `DeadLetterPublishingRecoverer`
- 固定退避（`FixedBackOff(backoffMillis, maxRetries)`）

策略：
- 可重试异常：数据库临时不可用、网络抖动
- 不可重试异常：参数非法、反序列化不可恢复错误（可直接进 DLT）

---

## 6. 现有代码改造点

## 6.1 `AuditLogServiceImpl`

保留当前事务后分发机制，仅替换 `dispatchPersist(auditLog)`：
- Kafka 开关开启：构建 `AuditLogEvent` 并发布
- Kafka 开关关闭：走原 `auditLogPersistService.persist(auditLog)`

## 6.2 `AuditLogPersistServiceImpl`

作为降级路径保留：
- 不删除、不破坏
- 在开关关闭或发布失败降级时兜底可用

---

## 7. 幂等与一致性策略（一期）

## 7.1 一致性策略

一期采用“事务后发送”弱一致：
- 主事务成功后才发送消息
- 发送失败通过日志告警 + 可选降级处理

## 7.2 幂等策略

建议在审计表中增加 `event_id` 唯一约束（或独立幂等表）。

好处：
- 防止重试/重复投递造成重复审计记录
- 便于 DLT 回放时安全重放

---

## 8. 部署与运行要求

## 8.1 Kafka Docker 要点

除端口映射外，还需确保：
- `advertised.listeners` 对应用可达
- broker 节点可通过 `bootstrap-servers` 正常连接

常见问题：
- 容器内部地址对宿主不可达，导致应用连接超时。

## 8.2 推荐环境变量

- `CHORDSKED_KAFKA_BOOTSTRAP_SERVERS`
- `CHORDSKED_KAFKA_AUDIT_ENABLED`
- `CHORDSKED_KAFKA_AUDIT_TOPIC`
- `CHORDSKED_KAFKA_AUDIT_GROUP_ID`

---

## 9. 验收清单

1. 触发一次包含审计记录的业务请求。
2. 在审计 topic 观察到消息。
3. Consumer 成功消费并写入审计表。
4. 人工制造消费异常后可见重试。
5. 重试耗尽后消息进入 DLT。
6. 关闭 Kafka 开关后，系统仍可通过本地异步路径记录审计。

---

## 10. 风险与回滚

风险点：
- Kafka 连接配置错误
- 消费端序列化/反序列化不一致
- 缺少幂等导致重复审计

回滚方案：
- 仅关闭 `chordsked.kafka.audit.enabled`
- 保留原本地异步持久化路径即可快速恢复

---

## 11. 二期建议（后续）

1. 引入 Outbox Pattern，提升“本地事务 + 消息投递”一致性。
2. 增加监控：consumer lag、DLT 堆积、发送失败率。
3. 增加 DLT 回放工具（按 eventId 精准重放）。
4. 扩展到通知、行为埋点等更多非核心异步链路。
