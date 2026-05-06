# Kafka 结构规范

## 1. 文档目标

本文档用于统一 `chordsked` 项目中 Kafka 的后端接入方式，明确以下内容：

- 什么时候适合使用 Kafka
- Producer / Consumer / Service 的职责边界
- 简单业务与复杂业务的推荐分层
- 包结构、命名方式、配置要求、异常处理规范
- 代码中应避免的反模式

本文档是“通用结构规范”，不是某一条具体业务链路的实施方案。

---

## 2. 总体设计原则

### 2.1 核心原则

Kafka 在项目中应被视为“基础设施适配层”，而不是业务层本体。

统一遵循以下原则：

1. 业务 Service 决定“什么时候发消息”，但不直接操作底层 `KafkaTemplate`
2. Kafka Producer 负责“怎么发消息”，对业务暴露语义化发布接口
3. Kafka Consumer 只负责“接消息、基础校验、日志、ack、重试入口”
4. 真正的业务处理必须下沉到 Service，而不是直接堆在 Consumer 中
5. DAO 只负责数据库读写，不直接承接 Kafka 消费入口
6. 优先按“业务域”划分消息模型、topic、consumer，而不是做一个大而全的通用 topic

### 2.2 为什么这样分层

这样设计有以下好处：

- 降低业务代码与 Kafka 的耦合
- 便于后续切换为 Outbox、MQ 替换、补偿机制升级
- 让 Consumer 保持轻量，便于排查和测试
- 避免每条链路都把消息细节、业务逻辑、数据库操作搅在一起
- 当多个业务都接入 Kafka 时，结构可以保持统一

---

## 3. 什么时候适合使用 Kafka

### 3.1 适合的场景

适合使用 Kafka 的场景：

- 异步解耦，如“业务完成后异步通知、异步审计、异步埋点”
- 吞吐较高，需要削峰填谷
- 上下游不要求强同步返回结果
- 允许最终一致性
- 有事件流语义，例如“订单已创建”“用户已注册”“审计已记录”

### 3.2 不适合的场景

不适合直接使用 Kafka 的场景：

- 强同步请求响应
- 主链路必须立即拿到处理结果
- 本地事务和消息发送必须强一致，但当前又未引入 Outbox / 事务消息
- 简单到一个本地异步线程池就足够的问题

### 3.3 一句话判断

如果业务诉求是“异步通知别人某件事已经发生”，优先考虑 Kafka。

如果业务诉求是“我要立刻拿到对方处理结果”，不要直接把 Kafka 当 RPC 用。

---

## 4. 标准分层模型

### 4.1 推荐链路

标准链路如下：

```text
Controller
    -> Service
        -> EventPublisher Interface
            -> Kafka Producer Adapter
                -> Kafka Topic
                    -> Kafka Consumer Adapter
                        -> Service
                            -> DAO
```

说明：

- 发送端由业务 Service 决定何时发消息
- Kafka Producer Adapter 负责真正发消息
- Kafka Consumer Adapter 负责真正接消息
- Service 负责消费后的业务处理
- DAO 只作为最终持久化层

### 4.2 职责边界

#### Service 的职责

- 决定是否发送消息
- 构建业务语义明确的事件对象
- 管理事务边界
- 编排消费后的业务逻辑
- 管理幂等、状态流转、补偿策略

#### Producer 的职责

- 对外暴露语义清晰的发布接口
- 封装 `KafkaTemplate`
- 处理发送日志、topic、key、发送回调
- 屏蔽底层 Kafka 细节

#### Consumer 的职责

- `@KafkaListener` 接收入站消息
- 做基础字段校验
- 记录消费日志
- 成功时 ack
- 失败时抛异常给统一错误处理器
- 委托 Service，而不是直接执行业务全流程

#### DAO 的职责

- 只做数据访问
- 不接 Kafka 语义
- 不接收消费入口直接调用来承担业务编排

---

## 5. 推荐包结构

按业务域组织 Kafka 代码，推荐结构如下：

```text
backend/src/main/java/com/chordsked/backend/
├── kafka/
│   ├── config/
│   │   ├── KafkaProducerConfig.java
│   │   ├── KafkaConsumerConfig.java
│   │   └── KafkaErrorHandlerConfig.java
│   ├── model/
│   │   ├── audit/
│   │   │   └── AuditLogEvent.java
│   │   └── order/
│   │       └── OrderCreatedEvent.java
│   ├── producer/
│   │   ├── AuditLogEventPublisher.java
│   │   ├── OrderEventPublisher.java
│   │   └── impl/
│   │       ├── AuditLogEventPublisherImpl.java
│   │       └── OrderEventPublisherImpl.java
│   └── consumer/
│       ├── AuditLogEventConsumer.java
│       └── OrderCreatedConsumer.java
└── service/
    ├── audit/
    │   ├── AuditLogService.java
    │   ├── AuditLogPersistService.java
    │   └── impl/
    └── order/
        ├── OrderService.java
        ├── OrderAsyncProcessService.java
        └── impl/
```

约束：

- `kafka/model` 只放消息对象
- `kafka/producer` 只放发布接口及实现
- `kafka/consumer` 只放监听适配器
- 核心业务逻辑仍然落在对应业务域的 `service`

---

## 6. 发送端规范

### 6.1 正确做法

业务 Service 不直接写：

```java
kafkaTemplate.send(...);
```

而是应依赖语义化接口：

```java
public interface OrderEventPublisher {
    void publishOrderCreated(OrderCreatedEvent event);
}
```

业务 Service 中调用：

```java
orderEventPublisher.publishOrderCreated(event);
```

### 6.2 发送端职责要求

Producer 需要负责：

- topic 选择
- key 选择
- 发送日志
- 发送结果回调
- 必要的空值校验

### 6.3 key 规范

消息 key 建议使用能表达业务聚合维度的主键：

- 审计日志：`eventId`
- 订单事件：`orderId`
- 用户事件：`userId`

目的：

- 便于排查
- 便于按 key 保序
- 便于定位具体消息

---

## 7. 消费端规范

### 7.1 Consumer 必须做什么

Consumer 应只做以下事情：

1. 接收消息
2. 基础校验
3. 打消费日志
4. 调用 Service
5. 成功后 ack
6. 失败时抛异常，由错误处理器接管

### 7.2 Consumer 不应该做什么

Consumer 不应该：

- 直接依赖 Mapper
- 写大量业务编排逻辑
- 同时处理多个无关业务
- 在方法里塞满 DAO 调用和状态流转
- 把异常吞掉后直接 ack

### 7.3 推荐结构

简单型业务：

```text
Consumer -> 业务已有 Service
```

复杂型业务：

```text
Consumer -> XxxConsumeService / XxxEventHandler -> XxxDomainService
```

这里的判断标准不是“是不是 Kafka”，而是“消费后的业务逻辑是否复杂”。

---

## 8. 简单型与复杂型业务的分层建议

### 8.1 简单型业务

简单型业务指消费后只需要做单一步骤处理，例如：

- 审计日志落库
- 埋点事件入库
- 简单通知记录

推荐结构：

```text
Consumer -> XxxService
```

例如当前审计链路：

```text
AuditLogEventConsumer -> AuditLogPersistService
```

这种结构有优势：

- 代码少
- 职责仍然清晰
- 不需要为每个业务机械新增一个很薄的 `EventService`

### 8.2 复杂型业务

复杂型业务指消费后需要多步编排，例如：

- 幂等校验
- 多表更新
- 状态流转
- 补偿逻辑
- 失败重试分类
- 调用多个领域服务

推荐结构：

```text
Consumer -> XxxConsumeService / XxxEventHandler -> XxxDomainService
```

例如：

```text
OrderCreatedConsumer
    -> OrderCreatedConsumeService
        -> OrderInventoryService
        -> OrderNotifyService
        -> OrderStatusService
```

### 8.3 项目统一建议

本项目推荐统一规则如下：

1. 默认优先 `Consumer -> 业务已有 Service`
2. 只有当消费逻辑明显复杂时，再拆 `ConsumeService` 或 `EventHandler`
3. 不要求每个 Kafka 业务都必须新建一个 `EventService`

---

## 9. 以审计链路为例的推荐结构

当前审计链路推荐结构如下：

```text
AuditLogService
    -> AuditLogEventPublisher
        -> Kafka
            -> AuditLogEventConsumer
                -> AuditLogPersistService
                    -> AuditLogDao
```

这条链路中：

- `AuditLogService` 负责主业务侧触发审计
- `AuditLogEventPublisher` 负责发 Kafka
- `AuditLogEventConsumer` 负责接消息和校验
- `AuditLogPersistService` 负责事件转实体并落库

该结构适合当前审计场景，因为消费后的业务处理很简单，不需要额外拆出单独的事件编排服务。

---

## 10. 配置规范

### 10.1 配置分层

Kafka 配置统一拆为两层：

- 业务配置：`chordsked.kafka.xxx`
- Spring Kafka 配置：`spring.kafka`

### 10.2 业务配置建议

每条业务链路建议至少具备：

- `enabled`
- `topic`
- `group-id`
- `concurrency`
- `max-retries`
- `backoff-millis`
- `dlt-suffix`

### 10.3 环境要求

- `bootstrap-servers` 必须由环境配置覆盖
- 禁止把线上地址写死在代码或公共配置里
- dev/test/prerelease/online 使用独立配置

---

## 11. 异常、重试与 DLT 规范

### 11.1 基本要求

消费失败不能简单吞掉，必须有明确处理策略：

- 可重试异常：交由错误处理器按策略重试
- 不可重试异常：直接进入 DLT 或记录后丢弃

### 11.2 推荐组件

推荐使用：

- `DefaultErrorHandler`
- `DeadLetterPublishingRecoverer`
- `FixedBackOff`

### 11.3 异常处理原则

- Consumer 中不要捕获后直接 `ack`
- 可恢复异常应抛出，交给 Kafka 错误处理器
- 不可恢复异常应明确分类
- 日志中必须带 `eventId`、topic、关键业务主键

---

## 12. 幂等规范

Kafka 天然可能出现重复投递、重复消费，因此消费侧必须考虑幂等。

推荐做法：

- 使用 `eventId` 作为幂等键
- 在业务表加唯一约束，或建立独立幂等表
- Service 内部先做幂等判断，再执行后续业务

对于审计日志这类场景，建议优先考虑：

- `event_id` 唯一约束
- 或独立消费记录表

---

## 13. 命名规范

### 13.1 事件类命名

- `AuditLogEvent`
- `OrderCreatedEvent`
- `UserRegisteredEvent`

规则：

- 用业务语义命名
- 表达“发生了什么”
- 不使用 `KafkaMessage1`、`CommonMessage` 这类无语义名称

### 13.2 Producer 命名

- `AuditLogEventPublisher`
- `OrderEventPublisher`

实现类：

- `AuditLogEventPublisherImpl`
- `OrderEventPublisherImpl`

### 13.3 Consumer 命名

- `AuditLogEventConsumer`
- `OrderCreatedConsumer`

### 13.4 消费处理服务命名

简单型：

- `AuditLogPersistService`
- `NotifyService`

复杂型：

- `OrderCreatedConsumeService`
- `UserRegisterEventHandler`

---

## 14. 反模式清单

以下写法不推荐：

### 14.1 业务 Service 直接依赖 `KafkaTemplate`

问题：

- 业务与基础设施强耦合
- 后续替换实现困难

### 14.2 Consumer 直接调用 Mapper

问题：

- 跳过 Service 层
- 事务、幂等、编排无统一出口

### 14.3 每个业务都强制新建一个很薄的 `EventService`

问题：

- 容易变成样板代码
- 增加文件数量但不增加真正价值

### 14.4 一个通用 topic 承载所有业务

问题：

- 消息模型混乱
- 消费者职责不清
- 演进困难

### 14.5 Consumer 吞异常后直接 ack

问题：

- 消息丢失
- 无法重试
- 现场难以追查

---

## 15. Kafka 接入步骤模板

新业务接入 Kafka 时，建议按以下步骤执行：

1. 明确业务是否真的需要 Kafka
2. 定义事件对象 `XxxEvent`
3. 定义 topic、group-id、重试配置
4. 新增 `Publisher` 接口及实现
5. 在业务 Service 中决定发送时机
6. 新增 `Consumer`
7. 由 `Consumer` 委托业务 Service 处理
8. 设计幂等方案
9. 配置重试与 DLT
10. 补充日志、监控、验收用例

---

## 16. 代码评审检查项

新增 Kafka 链路时，评审必须检查：

1. 业务 Service 是否绕过发布接口直接依赖 `KafkaTemplate`
2. Consumer 是否只做适配，还是塞入了大量业务逻辑
3. 是否直接 `Consumer -> DAO`
4. 是否有幂等设计
5. 是否有重试与 DLT 策略
6. topic、group-id、配置项是否环境化
7. 日志中是否包含关键消息标识
8. 是否按业务域拆分消息模型与消费者

---

## 17. 本项目统一结论

针对 `chordsked` 当前阶段，统一采用以下策略：

1. Kafka 作为基础设施层使用，不直接侵入业务层
2. 发送端统一采用 `Service -> Publisher Interface -> Producer Adapter`
3. 消费端默认采用 `Consumer -> 业务已有 Service`
4. 只有当消费逻辑复杂时，再拆 `ConsumeService / EventHandler`
5. 不要求每个业务都机械新增一个对应的 `EventService`
6. 禁止 `Consumer -> DAO` 作为默认架构

这样既能保持结构统一，也能避免过度设计，适合当前项目逐步扩展多个 Kafka 业务链路。
