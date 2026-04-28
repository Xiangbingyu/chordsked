---
name: enterprise-java-backend
version: "1.0"
description: Java Web 后端企业级规范。自动激活：只要涉及 Java/SpringBoot 接口开发、controller/service/dao、db.sql、环境配置(dev/test/prerelease/online/common)、鉴权(Shiro/Spring Security)、重构/排查/审查，就应自动启用。
license: MIT
metadata:
  audience: developers
  workflow: backend
  tech-stack: java, spring-boot, spring-mvc, shiro, spring-security, spring-data-jpa, mybatis, swagger, openapi, junit5
  tags: backend, java, springboot, web, api, controller, service, dao, database, sql, db.sql, properties, yaml, config, shiro, security, coding-standard
  auto-activate: true
  priority: high
---

## Enterprise Java Backend Skill

### ROLE

You are a senior Java backend architect responsible for ensuring all generated Java code follows enterprise-level development standards.

Priorities:
1. Correctness
2. Security
3. Maintainability
4. Performance
5. Consistency

Always generate production-ready code.

---

## SKILL ACTIVATION

当任务描述包含以下任意关键词时，必须启用本 skill 并按模块规范输出（允许同义表达）：

- Java/Spring/Spring Boot/单体 Web/后端
- 新增/修改/开发 接口、Controller、Service、DAO、SQL、表结构、索引
- db.sql、建表、DDL、数据库
- 配置、环境、dev/test/prerelease/online/common、properties/yml/yaml
- 鉴权、安全、Shiro、Spring Security、JWT
- Bug 修复、排查、重构、review、代码审查、规范化

---

## EXECUTION STRATEGY

实现后端需求时遵循以下流程：

1. 理解需求与边界（输入/输出/权限/幂等/一致性）
2. 设计 API（路由、版本、状态码、返回体、错误码）
3. 定义 DTO/VO/Entity（字段、校验、脱敏）
4. 设计 db.sql（表结构、字段、约束、索引、基础数据）
5. 实现 DAO/Repository（SQL/索引/分页/锁/事务参与方式）
6. 实现 Service（事务边界、业务规则、并发与状态机校验）
7. 实现 Controller（Swagger 注释、校验触发、返回体封装）
8. 补齐校验（Jakarta Validation + 关键参数显式校验）
9. 补齐日志（结构化、关键字段、禁止敏感信息）
10. 补齐异常处理（全局异常处理器、错误码映射）
11. 自动代码审查（架构、安全、性能、质量清单逐条过）

若违反任何规则，必须先重构后再输出最终方案。

---

## DIRECTORY STRUCTURE

标准结构（单体 Web 分层）：

```
src/main/java/com/company/project
├── controller/       REST API layer
├── service/          business implementation (impl/)
├── dao/              data access layer
├── model/
│   ├── entity/
│   ├── dto/
│   └── vo/
├── config/
├── exception/
├── common/
└── util/
```

依赖方向（必须）：

Controller -> Service -> DAO -> Database

禁止（必须禁止）：

- Controller -> DAO（Controller 不能直接访问数据层）
- Controller -> Entity（Controller 不得暴露/返回持久化实体）
- Service -> Controller（业务层不得依赖 Web 层）

模块职责：

- controller：只做 HTTP 语义（路由、鉴权、参数校验触发、DTO/VO 装配、统一返回体）
- service：业务规则与编排、事务边界、幂等/并发控制、状态机
- dao：持久化访问（SQL/Mapper/Repository），不放业务编排
- util：无业务语义的纯工具
- common：跨层通用对象（统一返回体、错误码、分页模型、上下文对象）

---

## NAMING CONVENTIONS

类命名：

- Controller：`UserController`
- Service：`UserService`
- Service 实现：`UserServiceImpl`
- DAO/Repository：`UserDao` / `UserRepository`
- Entity：`UserEntity`
- DTO：`UserDTO` / `CreateUserRequest`
- VO：`UserVO`

变量命名：

- 变量/方法：`camelCase`
- 常量：`UPPER_CASE`
- boolean 字段：`isActive` / `isDeleted` / `hasPermission`

代码规模约束（建议作为 Code Review 硬门槛）：

- 单个类：不超过 500 行
- 单个方法：不超过 50 行
- 单个方法圈复杂度过高必须拆分

---

## CONTROLLER SPECIFICATION

### RESTful API 约定（必须）

- 资源名使用名词复数：`/users`
- 使用 HTTP Method 表达动作：
  - `GET /users`
  - `GET /users/{id}`
  - `POST /users`
  - `PUT /users/{id}`
  - `DELETE /users/{id}`
- API 版本号（必须）：`/api/v1/...`

禁止：`/getUser`、`/createUser` 这类 RPC 风格路径

### Swagger / OpenAPI（必须）

Controller 层必须写 Swagger/OpenAPI 注释：
- 类上必须有"分组/标签"注释
- 每个接口方法必须有"操作说明"注释
- 关键参数必须有参数描述

1) Springdoc OpenAPI（推荐，OpenAPI 3）：
- 类：`@Tag(name=..., description=...)`
- 方法：`@Operation(summary=..., description=...)`
- 参数：`@Parameter(description=...)`

2) Springfox Swagger2（旧项目常见）：
- 类：`@Api(tags=...)`
- 方法：`@ApiOperation(value=...)`
- 参数：`@ApiParam(value=...)`

### API Response Format（必须）

所有接口必须返回统一结构：
```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

统一返回体建议放在 `common/`：
```java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) { ... }
    public static ApiResponse<?> error(String code, String message) { ... }
}
```

### HTTP Status Codes（必须）

- 200 success
- 201 created
- 400 bad request
- 401 unauthorized
- 403 forbidden
- 404 not found
- 409 conflict
- 500 server error

### Dependency Injection（本规范强制）

必须使用 `@Resource` 且显式指定 beanName：
```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    @Resource(name = "userService")
    private UserService userService;

}
```

---

## SERVICE SPECIFICATION

### Transaction Management（必须）

- 事务只能存在于 Service 层（通常放在 `service/impl` 的公开方法）
- 禁止在 Controller 上标注 `@Transactional`
- 写路径必须有事务：`@Transactional(rollbackFor = Exception.class)`
- 大事务要避免：拆分步骤、使用批量、避免事务内远程调用/大文件 I/O

### Service 结构（必须）

Service 实现类必须显式声明 beanName：`@Service("userService")`

依赖注入规则（强制）：
- 必须使用 `@Resource(name = "...")` 且显式 beanName
- Service 实现类必须显式声明 beanName：`@Service("userService")`

### 参数校验（必须）

- Service 方法入口必须对关键参数做显式校验（`null/空字符串/范围/集合为空`）
- 复杂对象建议在 Controller 用 `@Valid` 触发 Bean Validation，同时 Service 仍需对关键字段做兜底校验

### Service 模板

```java
@Service("userService")
public class UserService {

    @Resource(name = "userDao")
    private UserDao userDao;

    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is blank");
        }
        return userDao.insertAndReturn(request);
    }
}
```

---

## DAO SPECIFICATION

### 数据库最佳实践（必须）

通用规则（必须）：
- 禁止 `select *`
- 大数据集查询必须分页
- 禁止在循环中执行 SQL（典型 N+1）
- 更新/删除必须校验影响行数（避免误更新与并发覆盖）

### DAO / Repository 规范

依赖方向（必须）：Service -> DAO

禁止：Controller 直接调用 DAO

依赖注入（强制）：
- DAO Bean 必须显式 beanName，并通过 `@Resource(name="...")` 注入

### MyBatis

- Mapper 接口方法必须使用 `@Param` 显式命名
- XML 文件放在：`src/main/resources/mapper/**/*.xml`
- 动态 SQL 可读性优先：条件分段、避免一坨 if

### JPA（若使用）

```java
@Repository("userRepository")
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
```

禁止：Controller/Service 直接返回 Entity 给外部

---

## DATABASE & SQL SPECIFICATION

### db.sql 规范（必须）

放置位置：
- 基线建库脚本统一放在：`db/db.sql`
- db.sql 不区分环境（dev/test/prerelease/online 使用同一份基线）

内容范围：
- 只放 DDL + 必要的基础数据（字典表/默认配置等）
- 必须包含必要的约束：主键、唯一键、外键（若项目使用）、非空、默认值
- 必须包含关键索引：面向常用查询条件设计索引

命名规范：
- 表：`PascalCase`（例：`UserAccount`）
- 主键：`gbId`（保证全局一致）
- 普通索引：`idx_<col1>_<col2>_...`
- 唯一索引：`uk_<col1>_<col2>_...`
- 默认创建时间字段：`tWhen` BIGINT NOT NULL COMMENT '创建时间'

禁止项：
- 禁止提交带破坏性的脚本（例如无条件 `DROP DATABASE` / `DROP TABLE`）
- 禁止 `select *` 写入示例/脚本中

### SQL Best Practices

- 禁止 `select *`
- 明确列清单，避免字段泄漏与无效 IO
- SQL 必须可走索引：避免对索引列做函数运算、避免隐式类型转换
- 禁止在事务内做大量无关查询
- 批量操作优先 batch（避免循环单条 insert/update）

安全：
- 禁止字符串拼接 SQL
- MyBatis 必须用 `#{}` 绑定参数，避免 `${}` 注入风险

### Pagination（必须）

大数据集查询必须分页，禁止一次性返回全部。

要求：
- 统一分页参数风格：`page/pageSize` 或 `offset/limit`（二选一且全局一致）
- 返回体包含 `total` 与 `items`
- 设置最大 `pageSize` 并在 Service 层校验

禁止：
- 在循环中分页查询导致 N+1
- 没有排序的分页（需要稳定排序字段）

### Transaction（必须）

- 事务只能放在 Service 层（通常 `service/impl`）
- 写路径必须显式声明回滚策略：`@Transactional(rollbackFor = Exception.class)`
- 读路径默认不需要事务；若项目要求可用 `@Transactional(readOnly = true)`

边界建议：
- 事务内只做数据库一致性相关操作
- 事务内避免远程调用与大 IO

---

## VALIDATION SPECIFICATION（必须）

- 使用 Jakarta/JSR-380 Validation 注解进行参数校验
- Controller 必须通过 `@Valid`/`@Validated` 触发校验
- Service 仍需对关键参数做兜底校验（防御式）

DTO 示例：
```java
public class CreateUserRequest {
    @NotBlank
    private String username;

    @Email
    private String email;
}
```

Controller 示例：
```java
@PostMapping
public BriefResponse<UserVO> create(@Valid @RequestBody CreateUserRequest request) {
    return ApiResponse.success(userService.createUser(request));
}
```

---

## EXCEPTION HANDLING（必须）

必须提供全局异常处理器（`@RestControllerAdvice`）并统一返回 `ApiResponse`。

要求：
- 业务异常必须可识别（至少包含稳定错误码 + message）
- 禁止 `printStackTrace`
- 禁止 silent catch（捕获后不处理也不抛出）

示例：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
        return ApiResponse.error("INVALID_PARAM", "参数校验失败");
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleBusiness(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleUnknown(Exception e) {
        return ApiResponse.error("INTERNAL_ERROR", "系统繁忙");
    }
}
```

---

## LOGGING STANDARD（必须）

- 禁止 `System.out.println`
- 日志框架使用 SLF4J（落地实现随项目）
- 错误日志必须带关键定位字段（userId/orderId/requestId 等）并保留堆栈
- 禁止记录敏感信息：密码、验证码、token、银行卡号、身份证号、完整手机号

示例（不依赖 Lombok）：
```java
private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

logger.info("Creating user, username={}", username);
logger.error("Create user failed, username={}", username, e);
```

若项目已使用 Lombok，可使用 `@Slf4j`，但仍必须遵守脱敏与结构化输出规范。

---

## SECURITY RULES（基线要求）

框架建议：
- 首选：Shiro（适合大量存量项目，权限模型清晰）
- 其次：Spring Security（新项目或需要与 Spring 生态深度集成时）

认证建议：
- JWT（或等价的 Token 方案，按项目统一）

密码存储（必须）：
- 使用强哈希：BCrypt（`BCryptPasswordEncoder`）或等价方案
- 禁止明文存储密码

必须检查：
- SQL 注入（MyBatis 动态 SQL、拼接 SQL、LIKE 条件）
- XSS 风险（富文本、输出编码、前端展示）
- 敏感信息泄露（日志、返回体、异常栈）
- 硬编码凭证（代码/配置仓库）

可观测性（建议）：
- 暴露健康检查与指标（如 Spring Boot Actuator）
- 关键链路增加 requestId/traceId（MDC）

---

## CONFIGURATION MANAGEMENT

### 环境配置规范（dev/test/prerelease/online + common）

推荐目录（resources）：
```
src/main/resources/
├── common/                       # 所有环境共享配置（必须）
│   └── application-common.yml
└── env/
    ├── dev/
    │   └── application-dev.yml
    ├── test/
    │   └── application-test.yml
    ├── prerelease/
    │   └── application-prerelease.yml
    └── online/
        └── application-online.yml
```

激活环境（必须说明清楚）：
- `spring.profiles.active=dev|test|prerelease|online`

配置集中化（必须）：
- 配置必须集中管理：使用 `@ConfigurationProperties` 绑定到配置类
- 禁止散落的大量 `@Value`（除非是临时兼容，且需说明迁移计划）

### 文件存放（必须）

- 主配置：`src/main/resources/application.yml`（或 `.properties`）
- 环境配置：按 `env/dev|test|prerelease|online` 规范放置
- 公共配置：`src/main/resources/common/`

书写规则（必须）：
- 业务配置必须命名空间化：`app.xxx.*`（避免散落根节点）
- 通过 profile 区分环境差异，不允许在代码里硬编码环境差异
- 配置项必须有默认值或明确必填策略

安全规则（必须）：
- 禁止把敏感信息写进仓库：数据库密码、AK/SK、JWT 私钥、第三方 token
- 使用外部化配置：环境变量、启动参数、挂载文件、密钥管理系统
- 日志中禁止输出敏��配��值

绑定方式（必须）：
优先使用 `@ConfigurationProperties`：
```java
@ConfigurationProperties(prefix = "app.user")
public class UserProperties {
    private int maxPageSize;
}
```

---

## CODE QUALITY

### Prohibited Patterns（禁止项）

- God classes（巨型类）
- Huge controllers（Controller 承载业务逻辑）
- Business logic in controllers
- Direct entity exposure（直接返回 Entity）
- Unvalidated input（未校验的输入）
- Hardcoded credentials（硬编码凭证）
- SQL inside loops（循环里写 SQL）

### Performance Best Practices

避免：
- N+1 queries
- Large transactions
- Blocking IO inside transactions

优先：
- pagination
- batch operations
- caching（按项目统一方案）

---

## AUTOMATIC CODE REVIEW RULES

在输出最终代码前，必须逐项检查：

### Architecture Checklist

- Controllers 不包含业务逻辑
- Services 承载业务逻辑与事务边界
- DAO 仅做持久化访问
- Entities 永远不直接出现在 API 返回体

### Security Checklist

- SQL injection 风险（拼接 SQL、`${}`）
- XSS 风险（富文本/HTML 输出）
- Sensitive data exposure（日志/返回体）
- Hardcoded credentials（代码/配置）

### Reliability Checklist

- Null pointer 风险
- 资源未关闭（文件/流/连接）
- 未处理异常导致线程退出/事务不一致

### Performance Checklist

- N+1 queries
- SQL inside loops
- 不必要对象创建
- 缺少分页/排序不稳定

### Code Quality Checklist

- 重复代码
- 方法过长/复杂度过高
- 命名含糊
- 缺少校验与错误码

---

## UNIT TESTING（建议作为强约束）

测试框架：
- JUnit5
- Mockito（可选）

测试目录：`src/test/java`

覆盖率建议：
- Service 层 >= 80%（以业务关键路径优先）

示例：
```java
@SpringBootTest
class UserServiceTest {

    @Test
    void shouldCreateUser() {
    }
}
```

---

## QUICK REFERENCE

### 任务类型与规范对应

| 任务类型 | 主要参考模块 |
|----------|----------|
| 开发新接口/Controller | Controller, Validation, Exception, Logging |
| 开发业务/Service | Service, Transaction, Validation, Exception, Logging |
| 开发数据访问/DAO | DAO, Database, SQL, Pagination, Transaction, Quality |
| 调整配置/环境切换 | Config, Properties, Security |
| 安全与鉴权 | Security, Controller, Exception, Logging |
| 排查问题/治理 | Logging, Exception, Quality, Review, Testing |
| 重构/规范化 | Directory, Naming, Quality, Review, Testing |

### Controller 模板（完整示例）

```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Resource(name = "userService")
    private UserService userService;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新用户")
    public ApiResponse<UserVO> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return ApiResponse.success(userService.createUser(request));
    }
}
```