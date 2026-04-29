# sys_audit_log 设计与接入方案

## 1. 目标与边界

### 1.1 为什么要新增 `sys_audit_log`

当前项目的 [mysql/schema.sql](file:///e:/Github/chordsked/backend/src/main/resources/db/mysql/schema.sql) 和 [h2/schema.sql](file:///e:/Github/chordsked/backend/src/main/resources/db/h2/schema.sql) 中已经存在 `sys_operation_log`，它更适合记录常规接口访问与排障日志。

但根据 [04-审计日志模块设计](file:///e:/Github/chordsked/doc/一期/里程碑%201/架构设计/04-审计日志模块设计.md) 的定义，高风险、高敏感、高业务价值操作需要与普通操作日志严格分离，因此需要新增独立的 `sys_audit_log`：

- `sys_operation_log`：通用操作轨迹，偏排障与观察。
- `sys_audit_log`：高价值审计事件，偏合规、追责、长期检索。

角色创建、角色编辑、角色删除、权限分配、重置密码、导出敏感数据，都应优先进入 `sys_audit_log`。

### 1.2 设计约束

本方案按当前项目评审清单收口：

- 时间字段统一使用毫秒级 `BIGINT`，不使用 `DATETIME`
- 字段补齐 `COMMENT`
- 避免不必要的数据库默认值
- Service 不直接依赖 Mapper，必须通过 DAO Interface
- 审计日志写入不应让业务 Service 直接依赖 `HttpServletRequest`
- 普通运行日志与审计日志分离，`logger.info(...)` 不能替代审计日志落库

## 2. 建表 SQL

### 2.1 MySQL 建表 SQL

建议追加到 `backend/src/main/resources/db/mysql/schema.sql`：

```sql
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
    user_id BIGINT NULL COMMENT '操作人ID',
    user_name VARCHAR(50) NULL COMMENT '操作人名称',
    user_type VARCHAR(20) NULL COMMENT '账号体系(ADMIN/TEACHER/STUDENT)',
    module_name VARCHAR(50) NOT NULL COMMENT '业务模块名称',
    action_type VARCHAR(50) NOT NULL COMMENT '业务动作类型',
    biz_id BIGINT NULL COMMENT '关联业务主键ID',
    request_uri VARCHAR(255) NULL COMMENT '请求URI',
    request_method VARCHAR(10) NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    request_ip VARCHAR(50) NULL COMMENT '请求IP',
    user_agent VARCHAR(200) NULL COMMENT '请求User-Agent',
    request_params TEXT NULL COMMENT '核心请求参数(JSON)',
    response_result TEXT NULL COMMENT '核心响应结果(JSON/文本摘要)',
    status TINYINT NOT NULL COMMENT '执行状态(0:失败 1:成功)',
    error_msg VARCHAR(500) NULL COMMENT '失败原因摘要',
    created_at BIGINT NOT NULL COMMENT '创建时间(毫秒时间戳)',
    PRIMARY KEY (id),
    INDEX idx_sys_audit_log_user_id (user_id),
    INDEX idx_sys_audit_log_user_type (user_type),
    INDEX idx_sys_audit_log_module_action (module_name, action_type),
    INDEX idx_sys_audit_log_biz_id (biz_id),
    INDEX idx_sys_audit_log_status (status),
    INDEX idx_sys_audit_log_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '核心审计日志表';
```

### 2.2 H2 建表 SQL

建议同步追加到 `backend/src/main/resources/db/h2/schema.sql`：

```sql
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计日志ID',
    user_id BIGINT NULL COMMENT '操作人ID',
    user_name VARCHAR(50) NULL COMMENT '操作人名称',
    user_type VARCHAR(20) NULL COMMENT '账号体系(ADMIN/TEACHER/STUDENT)',
    module_name VARCHAR(50) NOT NULL COMMENT '业务模块名称',
    action_type VARCHAR(50) NOT NULL COMMENT '业务动作类型',
    biz_id BIGINT NULL COMMENT '关联业务主键ID',
    request_uri VARCHAR(255) NULL COMMENT '请求URI',
    request_method VARCHAR(10) NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    request_ip VARCHAR(50) NULL COMMENT '请求IP',
    user_agent VARCHAR(200) NULL COMMENT '请求User-Agent',
    request_params TEXT NULL COMMENT '核心请求参数(JSON)',
    response_result TEXT NULL COMMENT '核心响应结果(JSON/文本摘要)',
    status TINYINT NOT NULL COMMENT '执行状态(0:失败 1:成功)',
    error_msg VARCHAR(500) NULL COMMENT '失败原因摘要',
    created_at BIGINT NOT NULL COMMENT '创建时间(毫秒时间戳)',
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_sys_audit_log_user_id ON sys_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_user_type ON sys_audit_log(user_type);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_module_action ON sys_audit_log(module_name, action_type);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_biz_id ON sys_audit_log(biz_id);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_status ON sys_audit_log(status);
CREATE INDEX IF NOT EXISTS idx_sys_audit_log_created_at ON sys_audit_log(created_at);
```

### 2.3 字段设计说明

- `user_type` 使用 `VARCHAR(20)`，与当前权限表里的 `user_type` 一致，便于直接复用 `ADMIN/TEACHER/STUDENT`
- `status` 使用 `TINYINT`，与项目中 `sys_login_log.result` 的编码思路一致
- `created_at` 使用 `BIGINT`，符合当前评审清单，不沿用旧文档里的 `DATETIME`
- 不给 `status/created_at` 加数据库默认值，默认逻辑由代码控制
- 不建议给 `user_id` 建外键，避免用户删除、归档、跨端账号差异导致审计日志不可保留

## 3. 推荐 Java 分层设计

### 3.1 推荐新增文件

```text
backend/src/main/java/com/chordsked/backend/
├── model/
│   ├── entity/
│   │   └── AuditLogEntity.java
│   ├── dto/
│   │   └── audit/
│   │       └── AuditLogRecordRequest.java
│   └── enums/
│       └── AuditLogStatus.java
├── dao/
│   ├── AuditLogDao.java
│   ├── impl/
│   │   └── AuditLogDaoMBImpl.java
│   └── mapper/
│       └── AuditLogMapper.java
├── service/
│   └── audit/
│       ├── AuditLogService.java
│       └── impl/
│           └── AuditLogServiceImpl.java

backend/src/main/resources/mapper/audit/
└── AuditLogMapper.xml
```

### 3.2 Entity

参考现有 [LoginLogEntity](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/model/entity/LoginLogEntity.java) 风格，建议：

```java
package com.chordsked.backend.model.entity;

public class AuditLogEntity {
    private Long id;
    private Long userId;
    private String userName;
    private String userType;
    private String moduleName;
    private String actionType;
    private Long bizId;
    private String requestUri;
    private String requestMethod;
    private String requestIp;
    private String userAgent;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String errorMsg;
    private Long createdAt;

    // getter / setter
}
```

### 3.3 记录请求 DTO

业务 Service 不应直接依赖 Servlet，因此建议新增一个专门的记录请求 DTO，由业务只传业务信息，环境信息由审计模块统一补齐。

```java
package com.chordsked.backend.model.dto.audit;

public class AuditLogRecordRequest {
    private String moduleName;
    private String actionType;
    private Long bizId;
    private String requestParams;
    private String responseResult;
    private Integer status;
    private String errorMsg;

    // getter / setter
}
```

### 3.4 状态枚举

```java
package com.chordsked.backend.model.enums;

public enum AuditLogStatus {
    FAILED(0),
    SUCCESS(1);

    private final int code;

    AuditLogStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
```

## 4. DAO / Mapper 方案

### 4.1 DAO Interface

```java
package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.AuditLogEntity;

public interface AuditLogDao {
    int save(AuditLogEntity auditLog);
}
```

### 4.2 DAO 实现

```java
package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.AuditLogDao;
import com.chordsked.backend.dao.mapper.AuditLogMapper;
import com.chordsked.backend.model.entity.AuditLogEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("auditLogDao")
public class AuditLogDaoMBImpl implements AuditLogDao {
    @Resource(name = "auditLogMapper")
    private AuditLogMapper auditLogMapper;

    @Override
    public int save(AuditLogEntity auditLog) {
        if (auditLog == null) {
            return 0;
        }
        if (auditLog.getModuleName() == null || auditLog.getModuleName().isBlank()) {
            return 0;
        }
        if (auditLog.getActionType() == null || auditLog.getActionType().isBlank()) {
            return 0;
        }
        if (auditLog.getStatus() == null) {
            return 0;
        }
        if (auditLog.getCreatedAt() == null || auditLog.getCreatedAt() <= 0) {
            return 0;
        }
        return auditLogMapper.save(auditLog);
    }
}
```

### 4.3 Mapper Interface

```java
package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("auditLogMapper")
public interface AuditLogMapper {
    int save(@Param("auditLog") AuditLogEntity auditLog);
}
```

### 4.4 Mapper XML

建议放到 `backend/src/main/resources/mapper/audit/AuditLogMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.chordsked.backend.dao.mapper.AuditLogMapper">

    <insert id="save" parameterType="com.chordsked.backend.model.entity.AuditLogEntity">
        INSERT INTO sys_audit_log (
            user_id,
            user_name,
            user_type,
            module_name,
            action_type,
            biz_id,
            request_uri,
            request_method,
            request_ip,
            user_agent,
            request_params,
            response_result,
            status,
            error_msg,
            created_at
        ) VALUES (
            #{auditLog.userId},
            #{auditLog.userName},
            #{auditLog.userType},
            #{auditLog.moduleName},
            #{auditLog.actionType},
            #{auditLog.bizId},
            #{auditLog.requestUri},
            #{auditLog.requestMethod},
            #{auditLog.requestIp},
            #{auditLog.userAgent},
            #{auditLog.requestParams},
            #{auditLog.responseResult},
            #{auditLog.status},
            #{auditLog.errorMsg},
            #{auditLog.createdAt}
        )
    </insert>

</mapper>
```

## 5. AuditLogService 方案

### 5.1 Service Interface

建议保留“完整记录 + 成功简写 + 失败简写”三类入口：

```java
package com.chordsked.backend.service.audit;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;

public interface AuditLogService {
    void record(AuditLogRecordRequest request);

    void recordSuccess(String moduleName, String actionType, Long bizId, String requestParams, String responseResult);

    void recordFailure(String moduleName, String actionType, Long bizId, String requestParams, String errorMsg);
}
```

### 5.2 Service 实现职责

`AuditLogServiceImpl` 建议负责以下工作：

- 校验 `moduleName`、`actionType`、`status`
- 从 `SecurityContextHolder` 提取当前登录用户 `userId/userType`
- 通过 `RequestContextHolder` 提取 `requestUri/requestMethod/ip/userAgent`
- 在 `AuditLogServiceImpl` 内部集中完成上下文读取与实体组装，不再单独拆 `AuditLogContextResolver`
- 统一写入 `createdAt`
- 调用 `AuditLogDao.save(...)`
- 捕获审计写入异常并记录 `logger.error(...)`，但不影响主业务事务提交

## 6. 角色链路接入方案

### 6.1 接入原则

当前这 3 个方法：

- [RoleCreateServiceImpl.auditRoleCreated](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/service/role/impl/RoleCreateServiceImpl.java#L114-L122)
- [RoleUpdateServiceImpl.auditRoleUpdated](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/service/role/impl/RoleUpdateServiceImpl.java#L111-L119)
- [RoleDeleteServiceImpl.auditRoleDeleted](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/service/role/impl/RoleDeleteServiceImpl.java#L73-L75)

目前只是 `logger.info(...)`。

改造建议：

- 保留 `logger.info(...)` 作为普通运行日志
- 增加 `auditLogService.recordSuccess(...)` 作为真正审计日志入口
- 方法名可以继续叫 `auditRoleCreated` / `auditRoleUpdated` / `auditRoleDeleted`
- 但方法实现需要从“打日志”升级为“记录审计日志 + 运行日志”

### 6.2 模块名与动作类型建议

建议统一：

- `moduleName = "ROLE_MANAGEMENT"`
- `actionType = "CREATE_ROLE"`
- `actionType = "UPDATE_ROLE"`
- `actionType = "DELETE_ROLE"`

如果你更希望前端直接展示中文，也可以改为：

- `moduleName = "角色管理"`
- `actionType = "创建角色" / "编辑角色" / "删除角色"`

但从可维护性和枚举化角度，更建议先用稳定英文编码。

### 6.3 create 接入示例

```java
@Resource(name = "auditLogService")
private AuditLogService auditLogService;

private void auditRoleCreated(RoleEntity role, List<Long> permissionIds) {
    auditLogService.recordSuccess(
            "ROLE_MANAGEMENT",
            "CREATE_ROLE",
            role.getId(),
            "{\"roleId\":" + role.getId() + ",\"code\":\"" + role.getCode() + "\",\"permissionIds\":" + permissionIds + "}",
            "{\"status\":" + role.getStatus() + ",\"permissionCount\":" + permissionIds.size() + "}"
    );
    logger.info(
            "Role created, roleId={}, code={}, status={}, permissionCount={}",
            role.getId(),
            role.getCode(),
            role.getStatus(),
            permissionIds.size()
    );
}
```

### 6.4 update 接入示例

```java
@Resource(name = "auditLogService")
private AuditLogService auditLogService;

private void auditRoleUpdated(Long roleId, String code, RoleUpdateRequest request, List<Long> permissionIds) {
    auditLogService.recordSuccess(
            "ROLE_MANAGEMENT",
            "UPDATE_ROLE",
            roleId,
            "{\"roleId\":" + roleId + ",\"code\":\"" + code + "\",\"permissionIds\":" + permissionIds + "}",
            "{\"status\":" + request.getStatus() + ",\"permissionCount\":" + permissionIds.size() + "}"
    );
    logger.info(
            "Role updated, roleId={}, code={}, status={}, permissionCount={}",
            roleId,
            code,
            request.getStatus(),
            permissionIds.size()
    );
}
```

### 6.5 delete 接入示例

```java
@Resource(name = "auditLogService")
private AuditLogService auditLogService;

private void auditRoleDeleted(RoleEntity role) {
    auditLogService.recordSuccess(
            "ROLE_MANAGEMENT",
            "DELETE_ROLE",
            role.getId(),
            "{\"roleId\":" + role.getId() + ",\"code\":\"" + role.getCode() + "\"}",
            "{\"deleted\":true}"
    );
    logger.info("Role deleted, roleId={}, code={}", role.getId(), role.getCode());
}
```

### 6.6 请求参数记录建议

审计日志不建议无脑存整包请求体，建议只记录关键字段：

- 角色创建：`roleId`、`code`、`status`、`permissionIds`
- 角色编辑：`roleId`、`code`、`status`、变更后的 `permissionIds`
- 角色删除：`roleId`、`code`

不应记录：

- 明文密码
- 完整 token
- Cookie
- 大体积无关响应体

## 7. 推荐实现顺序

### 7.1 第一步

先落库：

- `db/mysql/schema.sql` 增加 `sys_audit_log`
- `db/h2/schema.sql` 同步增加 `sys_audit_log`

### 7.2 第二步

补基础分层：

- `AuditLogEntity`
- `AuditLogDao`
- `AuditLogDaoMBImpl`
- `AuditLogMapper`
- `AuditLogMapper.xml`

### 7.3 第三步

补 Service：

- `AuditLogRecordRequest`
- `AuditLogService`
- `AuditLogServiceImpl`

### 7.4 第四步

接入高价值操作：

- 角色创建
- 角色编辑
- 角色删除
- 用户角色分配
- 权限配置变更
- 密码重置

## 8. 是否要立即修改现有 3 个角色方法

结论：要改。

原因：

- 现在这 3 个方法只是在写普通应用日志
- 还没有落到 `sys_audit_log`
- 还不能满足“审计日志可检索、可留存、可追责”的目标

但改造方式不建议是“直接把 `logger.info(...)` 删除”，而应改成：

1. 继续保留普通运行日志
2. 增加 `auditLogService.recordSuccess(...)`
3. 后续再逐步抽成 `@AuditLog` 注解 + AOP

## 9. 最小可落地版本

如果想先快速上线第一版，建议最小范围如下：

- 仅新增 `sys_audit_log`
- 仅实现 `save` 能力，不先做分页查询接口
- 仅实现 `AuditLogService.recordSuccess(...)`
- 仅接入角色创建、编辑、删除 3 个主链路

这样改动最小，但已经能把高价值角色变更从普通日志里分离出来。
