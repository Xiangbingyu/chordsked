# RBAC 权限管理模块

## 1. 文档目标

本文档用于沉淀当前 ChordSked 里程碑 1 的 RBAC 模块设计、实现现状、分层规范、数据权限接入方式与后续开发顺序。

本文档以当前仓库真实代码状态为准，用于替代历史阶段性文档中的过时口径。

适用范围：

- 教务端固定权限树
- 教务端角色管理
- 教务端账号与角色/校区/数据范围联动
- 功能权限校验
- 数据权限隔离
- 安全缓存与权限装载
- H2 + Redis 集成测试标准

## 2. 当前结论

### 2.1 模块边界

里程碑 1 的 RBAC 模块采用以下固定边界：

- 权限树是系统预置元数据，不提供在线增删改
- 角色负责“功能权限”
- 用户负责“数据范围”
- 教务端使用完整 RBAC
- 教师端、学员端当前不开放角色管理页面
- 数据权限框架层保持通用，不只服务 internal/admin

### 2.2 当前统一口径

当前代码中已经统一为以下口径：

- 权限码统一带端前缀，如 `admin:user:view`
- 查询链路统一采用 `Request DTO + ResultVO`
- 数据范围主枚举统一使用 `UserDataScopeType`
- 数据权限查询链路统一接入 `@DataScope + /*DATA_SCOPE*/ + DataScopeInterceptor`
- 多实现动态分发统一采用 `接口 + Provider/Strategy + 路由表`

## 3. 当前已完成内容

### 3.1 已完成接口

当前已落地接口如下：

- `GET /admin/api/v1/permissions/tree`
- `GET /admin/api/v1/roles`
- `GET /admin/api/v1/roles/{roleId}`
- `POST /admin/api/v1/roles`
- `PUT /admin/api/v1/roles/{roleId}`
- `DELETE /admin/api/v1/roles/{roleId}`
- `GET /admin/api/v1/internal-users`
- `POST /admin/api/v1/internal-users`

### 3.2 已完成基础能力

当前已经具备：

- `SecurityConfig + JwtAuthenticationFilter + MultiAccountUserDetailsService`
- `InternalAccountProvider / TeacherAccountProvider / StudentAccountProvider`
- `SecurityCacheService` 用户快照缓存、权限缓存、token 状态缓存
- `UserDataScopeType` 统一数据范围枚举
- `DataScopeInterceptor` 占位符式数据权限拦截
- `DataScopeStrategy` 路由式策略结构
- `@Idempotent` 幂等防重能力（创建账号接口已接入）
- `@AuditLog + AuditLogAspect` 自动化审计记录
- `AuditLogPersistService + @Async` 审计异步持久化（线程池参数已配置化）
- `RequestIdFilter` 的 TraceId 兼容迁移（`X-Trace-Id` + `X-Request-Id`）
- `H2 + 真实 Redis` 的数据权限链路集成测试模板

## 4. 当前未完成接口

结合实现步骤文档与当前代码现状，仍未完成的教务端接口主要是账号命令链路：

- `PUT /admin/api/v1/internal-users/{userId}`
- `GET /admin/api/v1/internal-users/{userId}`
- `PUT /admin/api/v1/internal-users/{userId}/status`
- `PUT /admin/api/v1/internal-users/{userId}/reset-password`

其中当前最优先的是：

1. `PUT /admin/api/v1/internal-users/{userId}`
2. `PUT /admin/api/v1/internal-users/{userId}/status`
3. `PUT /admin/api/v1/internal-users/{userId}/reset-password`
4. `GET /admin/api/v1/internal-users/{userId}`

说明：

- 如果前端马上要做“账号创建/编辑”页面，优先继续做账号命令接口
- 如果前端先要做“账号详情回显/编辑页初始化”，则可把 `GET /admin/api/v1/internal-users/{userId}` 提前到第 2 位
- 但从当前整体规划与缺口看，最关键的仍然是把 `InternalUserCommandService` 整条命令链先补起来

## 5. 下一步应该做什么

### 5.1 推荐下一步

按照当前规划和真实代码进度，下一步建议直接实现：

`PUT /admin/api/v1/internal-users/{userId}`

原因：

- 角色管理已基本闭环
- 教务端账号列表与创建接口已经完成
- 当前账号模块最主要缺口已收敛到“编辑/状态/重置密码/详情”
- 先补编辑接口可以带出角色/校区/数据范围变更的缓存失效和审计回归

### 5.2 推荐实现顺序

建议按下面顺序继续开发：

1. `PUT /admin/api/v1/internal-users/{userId}`
2. `PUT /admin/api/v1/internal-users/{userId}/status`
3. `PUT /admin/api/v1/internal-users/{userId}/reset-password`
4. `GET /admin/api/v1/internal-users/{userId}`
6. 补齐缓存失效与审计日志回归测试

### 5.3 创建账号接口（已落地）的最小范围回顾

创建账号接口当前已覆盖：

- 用户名、手机号、角色、校区、数据范围校验
- 用户名唯一、手机号唯一校验
- 角色存在性与状态校验
- BCrypt 初始密码加密
- `sys_internal_user` 写入
- `sys_user_role` 写入
- `sys_user_campus` 写入
- 审计日志记录

首版不建议同时做得过重：

- 不要把所有命令接口一次性塞进一个大 Service
- 不要把缓存失效、审计日志、密码生成写成 Controller 逻辑
- 不要把数据权限写死在 Controller 或 XML 入参里

## 6. 当前推荐架构

### 6.1 分层方向

必须保持：

```text
Controller -> Service -> DAO -> Mapper -> Database
```

禁止：

- Controller 直接调 DAO
- Service 直接调 Mapper
- Controller 直接操作 Entity

### 6.2 命名口径

当前后续新增代码应统一使用：

- DTO：`InternalUserCreateRequest`、`InternalUserUpdateRequest`
- 查询 DTO：`InternalUserQueryRequest`
- 查询返回：`InternalUserQueryResultVO`
- 详情返回：`InternalUserDetailResultVO` 或同语义名称
- Service：按查询/命令拆分

### 6.3 多实现路由结构

对“按类型选择实现”的场景，统一采用：

```text
接口
  -> 多个 Provider/Strategy 实现
  -> 启动时收集 List<实现>
  -> 构建 路由表
  -> 入口类只负责路由与兜底
```

当前已经按该结构落地的链路：

- `MultiAccountUserDetailsService`
- `UserStatusVerificationServiceImpl`
- `DataScopeInterceptor + DataScopeStrategy`

后续类似场景应继续复用，不再回退到“注入多个具体实现 + if/else 分发”。

## 7. 数据权限设计

### 7.1 当前设计

当前数据权限已按以下方案落地：

- Mapper 方法上使用 `@DataScope`
- SQL 中必须显式包含 `/*DATA_SCOPE*/`
- `DataScopeInterceptor` 负责替换占位符
- `DataScopeStrategy` 根据 `UserDataScopeType` 生成条件
- `ChordSkedUserDetails` 承载当前用户数据范围信息

### 7.2 当前数据范围来源

当前阶段：

- 教务端账号：数据范围主来源是 `InternalUserEntity.dataScopeType`
- 教师端、学员端：当前按业务规则推导，框架保留扩展能力

### 7.3 当前固定映射

当前代码统一语义：

- `ALL` -> `UserDataScopeType.ALL_COMPANY`
- `CAMPUS` -> `UserDataScopeType.SPECIFIED_CAMPUS`
- `SELF` -> `UserDataScopeType.SELF_ONLY`

## 8. 资源目录与 Mapper 规范

当前资源目录已按领域收口：

- `resources/mapper/role/`
- `resources/mapper/user/`
- `resources/mapper/internal/`
- `resources/mapper/permission/`

要求：

- 同一张表尽量只维护一个 Mapper XML
- 目录名、namespace、DAO/Mapper 绑定关系必须一致
- 不允许同一张表在多个目录下保留重复 XML

## 9. 缓存与安全规则

### 9.1 权限与快照缓存

当前缓存职责分工：

- `SecurityCacheService`：权限码、用户快照、token 状态
- `UsernamePasswordLoginSnapshotCacheProvider`：登录快照
- 用户快照格式兼容策略：兼容 2 字段旧格式，3 字段及以上按前三字段解析

### 9.2 后续命令接口必须补的缓存动作

账号命令接口落地时，必须同步处理：

- 角色变更后清理权限缓存
- 用户状态变更后清理用户快照缓存
- 重置密码后清理登录快照和 token 状态
- 编辑账号角色/校区/数据范围后清理权限缓存与用户快照缓存

## 10. 测试标准

### 10.1 当前测试层次

当前 RBAC / DataScope 已具备三层测试：

- 单元测试
- 常规 H2 集成测试
- `H2 + 真实 Redis` 基础设施集成测试

### 10.2 当前正式标准

凡是涉及以下联动链路：

- 数据库
- Redis
- JWT 过滤器
- UserDetails 装载
- DataScope 拦截

都不能只依赖 mock，必须至少补一组正式基础设施集成测试。

测试标准：

- 数据库使用真实 H2 初始化脚本
- Redis 使用可连接的真实实例
- 真实请求经过过滤器与数据权限链路
- 至少覆盖冷缓存回源、热缓存命中、数据范围过滤三类场景

## 11. 后续接口开发清单

### 11.1 教务端账号命令链

建议新增：

- `service/internaluser/InternalUserCommandService.java`
- `service/internaluser/impl/InternalUserCommandServiceImpl.java`
- `model/dto/internaluser/InternalUserUpdateRequest.java`
- `model/dto/internaluser/InternalUserStatusUpdateRequest.java`
- `model/dto/internaluser/InternalUserResetPasswordRequest.java`

如需详情页，再补：

- `service/internaluser/InternalUserDetailQueryService.java`
- `model/dto/internaluser/InternalUserDetailQueryRequest.java`
- `model/vo/internaluser/InternalUserDetailResultVO.java`

### 11.2 账号命令接口开发顺序

推荐顺序：

1. 编辑账号
2. 状态变更
3. 重置密码
4. 详情查询

### 11.3 教务端账号接口职责与字段清单

本节用于明确教务端账号相关接口分别负责什么、应包含哪些字段，以及和当前已完成列表接口的边界差异。

#### 1. `GET /admin/api/v1/internal-users`

接口语义：

- 分页查询教务端账号列表
- 用于列表页、筛选页、表格页
- 返回摘要信息，不返回敏感信息和过重详情
- 查询结果必须经过 `DataScopeInterceptor` 自动过滤

当前列表接口建议返回字段：

- `id`
- `username`
- `phone`
- `name`
- `status`
- `dataScopeType`
- `primaryCampusId`
- `primaryCampusName`
- `roleIds`
- `createdAt`
- `updatedAt`

列表接口建议保留的筛选字段：

- `page`
- `pageSize`
- `keyword`
- `status`
- `roleId`
- `campusId`

列表接口不建议直接返回：

- `password`
- `mustChangePassword`
- 完整校区列表
- 完整角色对象列表
- 权限码列表

原因：

- 列表页目标是分页展示与筛选
- 详情页/编辑页需要的信息应由单独详情接口承载

#### 2. `GET /admin/api/v1/internal-users/{userId}`

接口语义：

- 查询单个教务端账号的完整详情
- 主要服务于详情页、编辑页初始化回显
- 详情查询同样必须经过数据范围校验

详情接口建议返回字段：

- `id`
- `username`
- `phone`
- `name`
- `avatar`
- `status`
- `mustChangePassword`
- `dataScopeType`
- `primaryCampusId`
- `campusIds`
- `roleIds`
- `createdAt`
- `updatedAt`

如前端需要减少二次请求，可进一步返回：

- `roles`
  - `id`
  - `code`
  - `name`
- `campuses`
  - `id`
  - `name`
  - `isPrimary`

详情接口不应返回：

- 密码明文
- BCrypt 密文
- 完整权限码列表

说明：

- 权限码本质上是角色派生能力，不是账号详情主字段
- 如果前端后续确实需要展示权限树，应单独评估是否扩展，而不是默认塞进账号详情接口

#### 3. `POST /admin/api/v1/internal-users`

接口语义：

- 创建教务端账号
- 同时完成角色绑定、校区绑定和数据范围落库

建议入参字段：

- `username`
- `phone`
- `name`
- `avatar` 可选
- `roleIds`
- `campusIds`
- `primaryCampusId`
- `dataScopeType`

建议由后端默认处理的字段：

- `password`
- `status`
- `mustChangePassword`
- `createdAt`
- `updatedAt`

建议规则：

- `status` 默认启用
- 初始密码由后端生成或按配置项给默认密码
- `mustChangePassword` 默认置为“是”

#### 4. `PUT /admin/api/v1/internal-users/{userId}`

接口语义：

- 修改一个具体教务端账号的信息
- 同时支持更新角色绑定、校区绑定和数据范围

建议入参字段：

- `userId`
- `phone`
- `name`
- `avatar`
- `roleIds`
- `campusIds`
- `primaryCampusId`
- `dataScopeType`

当前阶段不建议开放修改：

- `username`

原因：

- 修改用户名会影响登录快照缓存键、唯一性校验和审计语义
- 里程碑 1 建议先保持用户名稳定

#### 5. `PUT /admin/api/v1/internal-users/{userId}/status`

接口语义：

- 只修改用户状态
- 典型场景是启用/禁用

建议入参字段：

- `userId`
- `status`

建议状态值：

- `ENABLED`
- `DISABLED`

该接口不应承载：

- 角色更新
- 校区更新
- 数据范围更新

#### 6. `PUT /admin/api/v1/internal-users/{userId}/reset-password`

接口语义：

- 重置用户密码
- 当前阶段建议解释为“重置为系统默认初始密码或临时密码”

建议入参字段：

- `userId`
- `reason` 可选

建议后端处理：

- 生成默认密码或临时密码
- BCrypt 加密写库
- `mustChangePassword = YES`
- 清理登录快照
- 清理用户快照
- 撤销 token

建议实现口径：

- 默认密码不写死在 Service 中
- 统一走配置项

### 11.4 不同层的实现思路

#### 1. Controller 层

Controller 只负责：

- 路径参数和请求参数校验
- 组装 `Request DTO`
- 调用 Service
- 返回统一 `ApiResponse`

建议方法：

- `listInternalUsers()`
- `getInternalUserDetail()`
- `createInternalUser()`
- `updateInternalUser()`
- `updateInternalUserStatus()`
- `resetInternalUserPassword()`

#### 2. Service 层

建议按“查询”和“命令”拆分，不要混在一个大 Service 中。

查询服务建议：

- `InternalUserQueryService`
  - `list(InternalUserQueryRequest request)`
- `InternalUserDetailQueryService`
  - `getDetail(InternalUserDetailQueryRequest request)`

命令服务建议：

- `InternalUserCommandService`
  - `create(InternalUserCreateRequest request)`
  - `update(InternalUserUpdateRequest request)`
  - `updateStatus(InternalUserStatusUpdateRequest request)`
  - `resetPassword(InternalUserResetPasswordRequest request)`

#### 3. Service 函数职责建议

`create(...)`：

- 校验用户名唯一
- 校验手机号唯一
- 校验角色存在且启用
- 校验校区集合与主校区关系
- 校验 `dataScopeType`
- 生成并加密初始密码
- 保存用户
- 保存用户角色
- 保存用户校区
- 记录审计日志

`getDetail(...)`：

- 校验 `userId`
- 查询用户基础信息
- 查询角色绑定
- 查询校区绑定
- 组装详情 VO

`update(...)`：

- 校验目标用户存在
- 校验目标用户是否在当前操作者可操作范围内
- 校验手机号唯一
- 校验角色、校区、主校区、数据范围
- 更新基础信息
- 覆盖更新角色绑定
- 覆盖更新校区绑定
- 清理权限缓存与用户快照缓存
- 记录审计日志

`updateStatus(...)`：

- 校验目标用户存在
- 校验状态合法
- 校验不能禁用自己
- 校验最后一个管理员保护逻辑
- 更新状态
- 清理用户快照缓存
- 撤销 token
- 记录审计日志

`resetPassword(...)`：

- 校验目标用户存在
- 校验目标用户在可操作范围内
- 生成默认密码/临时密码
- BCrypt 加密
- 标记强制改密
- 清理登录态
- 清理快照缓存
- 撤销 token
- 记录审计日志

#### 4. DAO 层

当前 `InternalUserDao` 已有：

- `getById(Long userId)`
- `getByUsername(String username)`
- `listByQuery(InternalUserQueryRequest request)`
- `countByQuery(InternalUserQueryRequest request)`

后续命令链建议补充：

- `getByPhone(String phone)`
- `save(InternalUserEntity user)`
- `updateById(InternalUserEntity user)`
- `updateStatus(Long userId, Integer status, Long updatedAt)`
- `updatePassword(Long userId, String password, Integer mustChangePassword, Long updatedAt)`

配套还需要复用或补齐：

- `UserRoleDao`
  - `listByUserIds(...)`
  - `deleteByUserId(...)`
  - `saveBatch(...)`
- `UserCampusDao`
  - `getPrimaryCampusIdByUserId(...)`
  - `listCampusIdsByUserId(...)`
  - `deleteByUserId(...)`
  - `saveBatch(...)`

#### 5. Mapper / XML 层

查询列表已经走：

- `@DataScope`
- `/*DATA_SCOPE*/`
- `DataScopeInterceptor`

详情查询建议也采用相同模式：

- Mapper 方法标注 `@DataScope`
- SQL 中保留 `/*DATA_SCOPE*/`
- 让“列表查不到”和“详情查不到”保持同一套数据范围规则

## 12. 当前与历史文档差异

下列历史口径已不再作为当前标准：

- `InternalUserPageRequest` / `InternalUserPageItemVO`
- `InternalUserDataScopeType`
- 依赖拦截器猜测 `WHERE / ORDER BY / LIMIT` 的 DataScope 方案
- 多策略直接注入后用 `if/else` 分发的入口写法
- 重复的 mapper 目录与不一致 namespace

当前应以本文件和 `.trae/docs/代码评审检查清单.md` 为准。

## 13. 最终建议

如果你接下来继续实现未完成接口，建议直接进入：

### 第一步

实现 `PUT /admin/api/v1/internal-users/{userId}`

### 第二步

补 `InternalUserCommandService` 与对应 DTO / DAO / 测试

### 第三步

以同样模式继续完成：

- 编辑账号
- 状态变更
- 重置密码
- 详情查询

这样可以和当前已经完成的：

- 权限树
- 角色 CRUD
- 账号列表
- 账号创建
- DataScope 框架

顺利拼成里程碑 1 的完整 RBAC 主链路。
