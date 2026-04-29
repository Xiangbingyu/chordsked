---
name: enterprise-java-backend
description: Java Web 后端企业级规范。自动激活：只要涉及 Java/SpringBoot 接口开发、controller/service/dao、db.sql、环境配置(dev/test/prerelease/online/common)、鉴权(Shiro/Spring Security)、重构/排查/审查，就应自动启用。
TRIGGER when: code file ends with .java and involves Spring Boot, Controller, Service, DAO, MyBatis, JPA, or user asks for backend API development, database design, or Java enterprise patterns
SKIP: file ends with .ts, .tsx, .js, .jsx, .py, .go, or user explicitly requests other languages/frameworks
---

# Enterprise Java Backend Skill

## ROLE

你是资深 Java 后端架构师，目标是保证输出的代码可直接用于生产环境。

优先级：

1. Correctness
2. Security
3. Maintainability
4. Performance
5. Consistency

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

---

## ARCHITECTURE RULES

所有后端项目必须遵循分层架构（Layered Architecture）。

标准结构（单体 Web）：

```
src/main/java/com/company/project
├── controller/       REST API layer
├── service/          business implementation
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

规模约束：

- 单个类：不超过 500 行
- 单个方法：不超过 50 行
- 单个方法圈复杂度过高必须拆分

---

## API DESIGN STANDARD

RESTful API 约定（必须）：

- 资源名使用名词复数：`/users`
- 使用 HTTP Method 表达动作：
  - `GET /users`
  - `GET /users/{id}`
  - `POST /users`
  - `PUT /users/{id}`
  - `DELETE /users/{id}`
- API 版本号（必须）：`/api/v1/...`

禁止：`/getUser`、`/createUser` 这类 RPC 风格路径

### SWAGGER / OPENAPI（必须）

Controller 层必须写 Swagger/OpenAPI 注释：

- 类上必须有"分组/标签"注释
- 每个接口方法必须有"操作说明"注释
- 关键参数必须有参数描述

Springdoc OpenAPI（推荐）：

- 类：`@Tag(name=..., description=...)`
- 方法：`@Operation(summary=..., description=...)`
- 参数：`@Parameter(description=...)`

### API RESPONSE FORMAT（必须）

所有接口必须返回统一结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### HTTP STATUS CODES（必须）

- 200 success
- 201 created
- 400 bad request
- 401 unauthorized
- 403 forbidden
- 404 not found
- 409 conflict
- 500 server error

### DEPENDENCY INJECTION（强制）

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

## SERVICE LAYER

### TRANSACTION MANAGEMENT（必须）

- 事务只能存在于 Service 层
- 禁止在 Controller 上标注 `@Transactional`
- 写路径必须有事务：`@Transactional(rollbackFor = Exception.class)`
- 大事务要避免：拆分步骤、使用批量、避免事务内远程调用/大文件 I/O

### SERVICE 结构（必须）

- `service`：实现（事务、编排、持久化调用）

依赖注入规则（强制）：

- 必须使用 `@Resource(name = "...")` 且显式 beanName
- Service 实现类必须显式声明 beanName：`@Service("userService")`

### 参数校验（必须）

- Service 方法入口必须对关键参数做显式校验（`null/空字符串/范围/集合为空`）
- 复杂对象建议在 Controller 用 `@Valid` 触发 Bean Validation

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

## DAO LAYER

### DATABASE BEST PRACTICES（必须）

支持的持久化方案：MyBatis

通用规则（必须）：

- 禁止 `select *`
- 大数据集查询必须分页
- 禁止在循环中执行 SQL（典型 N+1）
- 更新/删除必须校验影响行数

### MyBatis

- Mapper 接口方法必须使用 `@Param` 显式命名
- XML 文件放在：`src/main/resources/mapper/**/*.xml`
- 动态 SQL 可读性优先：条件分段、避免一坨 if

---

## DATABASE SCHEMA

### db.sql 规范（必须）

放置位置：

- 基线建库脚本统一放在：`db/db.sql`
- db.sql 不区分环境

内容范围：

- 只放 DDL + 必要的基础数据
- 必须包含必要的约束：主键、唯一键、外键、非空、默认值
- 必须包含关键索引

命名规范：

- 表：`PascalCase`（例：`UserAccount`）
- 主键：`gbId`
- 普通索引：`idx_<col1>_<col2>_...`
- 唯一索引：`uk_<col1>_<col2>_...`
- 默认创建时间字段：`tWhen BIGINT NOT NULL COMMENT '创建时间'`

禁止项：

- 禁止提交带破坏性的脚本
- 禁止 `select *` 写入示例/脚本中

---

## VALIDATION RULES（必须）

- 使用 Jakarta/JSR-380 Validation 注解
- Controller 必须通过 `@Valid`/`@Validated` 触发校验
- Service 仍需对关键参数做兜底校验

DTO 示例：

```java
public class CreateUserRequest {
    @NotBlank
    private String username;

    @Email
    private String email;
}
```

---

## EXCEPTION HANDLING（必须）

必须提供全局异常处理器（`@RestControllerAdvice`）并统一返回 `ApiResponse`。

要求：

- 业务异常必须可识别（至少包含稳定错误码 + message）
- 禁止 `printStackTrace`
- 禁止 silent catch

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
}
```

---

## LOGGING STANDARD（必须）

- 禁止 `System.out.println`
- 日志框架使用 SLF4J
- 错误日志必须带关键定位字段
- 禁止记录敏感信息：密码、验证码、token、银行卡号、身份证号

```java
private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

logger.info("Creating user, username={}", username);
logger.error("Create user failed, username={}", username, e);
```

---

## SECURITY RULES（基线要求）

框架建议：

- 首选：Shiro
- 其次：Spring Security

认证建议：JWT

密码存储（必须）：

- 使用强哈希：BCrypt
- 禁止明文存储密码

必须检查：

- SQL 注入（MyBatis 动态 SQL、拼接 SQL、LIKE 条件）
- XSS 风险（富文本、输出编码、前端展示）
- 敏感信息泄露（日志、返回体、异常栈）
- 硬编码凭证（代码/配置仓库）

---

## CONFIGURATION MANAGEMENT

### 环境配置规范

推荐目录：

```
src/main/resources/
├── common/
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

激活环境：`spring.profiles.active=dev|test|prerelease|online`

### 书写规则（必须）

- 业务配置必须命名空间化：`app.xxx.*`
- 通过 profile 区分环境差异
- 配置项必须有默认值或明确必填策略

### 安全规则（必须）

- 禁止把敏感信息写进仓库
- 使用外部化配置：环境变量、启动参数、挂载文件

---

## PAGINATION（必须）

大数据集查询必须分页。

要求：

- 统一分页参数风格：`page/pageSize` 或 `offset/limit`
- 返回体包含 `total` 与 `items`
- 设置最大 `pageSize` 并在 Service 层校验

禁止：

- 在循环中分页查询导致 N+1
- 没有排序的分页

---

## SQL BEST PRACTICES

- 禁止 `select *`
- SQL 必须可走索引
- 禁止在事务内做大量无关查询
- 批量操作优先 batch

安全：

- 禁止字符串拼接 SQL
- MyBatis 必须用 `#{}` 绑定参数

---

## TESTING

测试框架：JUnit5 + Mockito

测试目录：`src/test/java`

覆盖率建议：Service 层 >= 80%

```java
@SpringBootTest
class UserServiceTest {

    @Test
    void shouldCreateUser() {
    }
}
```

---

## CODE REVIEW CHECKLIST

### ARCHITECTURE

- Controllers 不包含业务逻辑
- Services 承载业务逻辑与事务边界
- DAO 仅做持久化访问
- Entities 永远不直接出现在 API 返回体

### SECURITY

- SQL injection 风险
- XSS 风险
- Sensitive data exposure
- Hardcoded credentials

### RELIABILITY

- Null pointer 风险
- 资源未关闭
- 未处理异常

### PERFORMANCE

- N+1 queries
- SQL inside loops
- 缺少分页/排序不稳定

### CODE QUALITY

- 重复代码
- 方法过长/复杂度过高
- 命名含糊
- 缺少校验与错误码

---

## PROHIBITED PATTERNS（禁止项）

- God classes（巨型类）
- Huge controllers
- Business logic in controllers
- Direct entity exposure
- Unvalidated input
- Hardcoded credentials
- SQL inside loops
