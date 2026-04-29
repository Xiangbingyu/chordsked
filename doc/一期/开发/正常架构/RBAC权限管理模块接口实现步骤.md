# RBAC 权限管理模块接口实现步骤

## 1. 文档目标

本文档用于指导 ChordSked 里程碑 1 中 RBAC 权限管理模块的后端实现，覆盖以下层次：

- `controller` 层
- `service` 层
- `security` 层
- `cache` 层
- `utils` 层
- `dao` 层
- `model` 层
- `db/sql` 初始化与迁移
- 测试与验收

本文档综合以下来源：

- `doc/一期/里程碑 1/需求文档/02-授权模块需求.md`
- `doc/一期/里程碑 1/需求文档/03-教务端账号管理需求.md`
- `doc/一期/里程碑 1/需求文档/04-角色管理需求.md`
- `doc/一期/里程碑 1/需求文档/08-账号权限体系需求.md`
- `doc/一期/里程碑 1/需求文档/权限点清单.md`
- `doc/一期/里程碑 1/架构设计/02-授权与数据隔离设计.md`
- `.trae/docs/代码评审检查清单.md`

## 2. 设计结论

### 2.1 模块边界

里程碑 1 的 RBAC 模块采用“固定权限树 + 角色分配权限 + 用户绑定角色”的模式。

结论如下：

- `sys_permission` 中的权限节点属于系统预置元数据
- 里程碑 1 不做“权限节点在线增删改”
- 后台页面主要实现的是“角色权限配置”，不是“权限树节点管理 CMS”
- 教务端使用 RBAC 做功能权限控制
- 教师端不做角色配置，默认拥有教师端页面权限，但数据权限受限
- 学员端不做 RBAC 页面权限控制
- 当前阶段数据范围采用“挂用户”方案，教务端账号自身维护数据范围类型

### 2.2 权限树维护策略

结合需求文档与代码评审检查清单，权限树建议采用以下维护方式：

- 权限树的源定义以代码中的常量树或枚举树为准，数据库只作为运行时查询与角色授权存储
- 权限点文档来源于需求文档与权限点清单，代码定义必须与文档保持同步
- 首次部署或环境初始化时，通过 SQL 种子数据把代码权限树导入 `sys_permission`
- 权限点变更流程为：改需求文档 -> 改权限点清单 -> 改代码权限树 -> 更新 SQL 种子数据 -> 改接口权限注解
- 不提供权限节点创建、编辑、删除接口
- 若未来权限树进入高频业务变更阶段，应继续坚持“代码定义为准，数据库为投影”的原则

### 2.3 当前项目基础

当前项目已经具备以下基础能力：

- `security` 登录主链路已存在
- `AccountProvider` 多账号装载机制已存在
- `AuthorityCodeService` 已接入数据库权限查询
- `PermissionEntity`、`RolePermissionEntity`、`PermissionDao`、`RolePermissionDao` 已具备基础访问能力
- `SecurityCacheService` 已具备用户快照和权限码缓存基础能力
- `InternalUserEntity.dataScopeType` 已提供教务端账号级的数据范围字段
- `DataScopeEntity` 已存在通用数据范围模型，但当前阶段尚未作为主落地模型使用

当前需要继续补齐的是：

- 角色管理接口
- 权限树查询接口
- 角色权限回显与保存接口
- 教务端账号角色绑定接口
- 权限缓存失效机制
- 审计日志记录

### 2.4 文档阅读顺序建议

为了避免“先看到局部实现、后看到联动约束”造成理解跳跃，建议按以下顺序阅读本文档：

1. `第 2 章`：先看整体边界、固定权限树策略和当前项目基础
2. `第 3 章`：再看统一路由、分层和命名规范
3. `第 4 章`：明确最终要交付哪些接口
4. `第 8 章`：理解安全主链路、接口鉴权与缓存装载
5. `第 12 章`：理解功能权限与数据范围是两套校验机制
6. `第 5-11 章`：按分层查看 model、dao、cache、utils、service、controller 的实现要点
7. `第 13-16 章`：最后查看 SQL 初始化、实施顺序、测试清单和验收标准

章节职责建议理解为：

- `第 4 章` 解决“要做哪些接口”
- `第 5-11 章` 解决“每一层怎么做”
- `第 12 章` 解决“功能权限和数据权限如何联动”
- `第 14 章` 解决“实际开发先做什么、后做什么”

## 3. 路由与命名约束

### 3.1 路由规则

必须遵循 `.trae/docs/代码评审检查清单.md`：

- 教务端统一前缀：`/admin/api/v1`
- 教师端统一前缀：`/teachers/api/v1`
- 学员端统一前缀：`/students/api/v1`

RBAC 模块属于教务端配置能力，因此所有角色管理和权限配置接口都放在：

```text
/admin/api/v1
```

### 3.2 分层约束

必须遵循以下依赖方向：

```text
Controller -> Service -> DAO -> Mapper -> Database
```

禁止：

- Controller 直接调 DAO
- Service 直接调 Mapper
- Controller 直接操作 Entity

### 3.3 角色与权限命名规则

必须严格对齐代码评审检查清单：

- 角色代码统一使用业务角色编码：`ADMIN`、`OPERATOR`、`TEACHER`、`STUDENT`
- `admin:role` 作为教务端管理接口的基础入口权限，所有 `/admin/api/v1/**` 请求都必须先通过该权限校验
- 细粒度接口权限统一使用权限码，不允许在 RBAC 接口里混用 `ROLE_ADMIN` 和 `ADMIN`
- 教务端 RBAC 接口优先使用 `hasAuthority(...)`，不使用 `hasRole('ADMIN')` 作为细粒度权限校验手段
- 如确有基础角色校验需求，也必须明确约定一套固定映射规则，避免同时出现 `ROLE_ADMIN` 和数据库角色编码 `ADMIN` 并行扩散

### 3.4 权限码规则

结合检查清单建议，里程碑 1 统一使用带端前缀的权限码：

```text
{client}:{resource}:{action}
```

教务端示例：

- `admin:user:view`
- `admin:user:create`
- `admin:user:update`
- `admin:user:enable`
- `admin:user:reset_password`
- `admin:role:view`
- `admin:role:create`
- `admin:role:update`
- `admin:role:delete`
- `admin:role:assign_permission`

教师端和学员端如后续需要细粒度权限，也沿用同一规范：

- `teacher:schedule:view`
- `teacher:homework:update`
- `student:course:view`

里程碑 1 中不得再使用不带端前缀的 `user:view`、`role:create` 作为新规范示例。

## 4. 模块目标接口清单

本章所有教务端接口统一约定：

- 先通过基础入口权限 `hasAuthority('admin:role')`
- 再通过各接口自己的细粒度权限校验
- 下文“鉴权”项仅列出每个接口额外要求的细粒度权限

### 4.1 权限树查询接口

#### 1. 查询固定权限树

- 方法：`GET`
- 路径：`/admin/api/v1/permissions/tree`
- 作用：查询系统预置权限树，供角色权限配置页面展示
- 鉴权：`hasAuthority('admin:role:view')` 或 `hasAuthority('admin:role:assign_permission')`

#### 2. 角色权限回显与保存说明

- 角色权限回显已并入 `GET /admin/api/v1/roles/{roleId}`
- 角色权限保存已并入 `PUT /admin/api/v1/roles/{roleId}`
- 不再单独提供 `/admin/api/v1/roles/{roleId}/permissions` 链路
- 原因：角色编辑页需要一次性拿到角色基础信息、统计信息、已分配权限和完整权限树，适合按完整角色资源返回

### 4.2 角色管理接口

#### 4. 角色分页列表

- 方法：`GET`
- 路径：`/admin/api/v1/roles`
- 作用：分页查询角色列表，返回权限数量、关联用户数量、状态
- 鉴权：`hasAuthority('admin:role:view')`

#### 5. 角色详情

- 方法：`GET`
- 路径：`/admin/api/v1/roles/{roleId}`
- 作用：查询角色完整详情，返回 `id`、`code`、`name`、`description`、`status`、`permissionCount`、`userCount`、`permissionIds`、`permissionTree`、`createdAt`、`updatedAt`
- 鉴权：`hasAuthority('admin:role:view')`

#### 6. 创建角色

- 方法：`POST`
- 路径：`/admin/api/v1/roles`
- 作用：创建角色并可同时绑定权限
- 鉴权：`hasAuthority('admin:role:create')`

#### 7. 编辑角色

- 方法：`PUT`
- 路径：`/admin/api/v1/roles/{roleId}`
- 作用：更新角色名称、描述、状态和权限配置
- 鉴权：`hasAuthority('admin:role:update')`

#### 8. 删除角色

- 方法：`DELETE`
- 路径：`/admin/api/v1/roles/{roleId}`
- 作用：删除角色
- 鉴权：`hasAuthority('admin:role:delete')`

### 4.3 教务端账号角色绑定接口

角色与数据权限分开管理，因此账号侧仍需实现角色绑定，并直接维护账号自己的数据范围类型。

本节只定义账号侧接口边界。

数据范围相关详细规则不在本节重复展开，统一以下列章节为准：

- `12.2 数据权限`
- `12.3 数据范围类型与当前阶段口径`
- `12.4 数据范围校验责任划分`
- `12.6 CAMPUS 与 ASSIGNED 的边界说明`

#### 9. 创建教务端账号

- 方法：`POST`
- 路径：`/admin/api/v1/internal-users`
- 作用：创建教务端账号，绑定角色和校区
- 鉴权：`hasAuthority('admin:user:create')`

#### 10. 编辑教务端账号

- 方法：`PUT`
- 路径：`/admin/api/v1/internal-users/{userId}`
- 作用：更新账号信息、角色、校区绑定
- 鉴权：`hasAuthority('admin:user:update')`

#### 11. 教务端账号列表

- 方法：`GET`
- 路径：`/admin/api/v1/internal-users`
- 作用：分页查询教务端账号列表
- 鉴权：`hasAuthority('admin:user:view')`

#### 12. 教务端账号启用/禁用

- 方法：`PUT`
- 路径：`/admin/api/v1/internal-users/{userId}/status`
- 作用：启用/禁用账号，并使登录态失效
- 鉴权：`hasAuthority('admin:user:enable')`

#### 13. 重置密码

- 方法：`PUT`
- 路径：`/admin/api/v1/internal-users/{userId}/reset-password`
- 作用：重置密码并强制用户重新登录
- 鉴权：`hasAuthority('admin:user:reset_password')`

## 5. Model 层实现步骤

### 5.1 已有实体

当前已存在：

- `PermissionEntity`
- `RolePermissionEntity`
- `RoleEntity`
- `UserRoleEntity`
- `InternalUserEntity`
- `UserCampusEntity`
- `DataScopeEntity`

当前阶段的建模口径说明：

- `InternalUserEntity.dataScopeType` 是教务端账号数据范围的当前主来源
- `UserCampusEntity` 用于承载账号绑定校区等范围边界
- `DataScopeEntity` 暂不作为里程碑 1 教务端账号数据范围的主表
- `DataScopeEntity` 更适合作为未来“通用授权对象范围配置”扩展模型，例如 `ASSIGNED`、按对象分配、按主体覆盖等场景

### 5.2 需要补齐的 DTO

建议新增以下 DTO：

```text
model/dto/role/RolePageRequest.java
model/dto/role/RoleDetailQueryRequest.java
model/dto/role/RoleCreateRequest.java
model/dto/role/RoleUpdateRequest.java
model/dto/permission/PermissionTreeQueryRequest.java
model/dto/internaluser/InternalUserCreateRequest.java
model/dto/internaluser/InternalUserUpdateRequest.java
model/dto/internaluser/InternalUserPageRequest.java
model/dto/internaluser/InternalUserStatusUpdateRequest.java
```

关键要求：

- Controller 入参必须使用 `jakarta.validation` 注解
- 分页参数要做边界校验
- `permissionIds` 不能为空集合时要显式校验
- `roleCode`、`roleName`、`description` 要限制长度
- 教务端账号相关 DTO 需要显式包含 `dataScopeType`
- 查询/编辑链路统一使用 `Request DTO + ResultVO` 风格；即使详情接口当前只有 `roleId`，也建议保留 `RoleDetailQueryRequest` 保持结构一致

### 5.3 需要补齐的 VO

建议新增以下 VO：

```text
model/vo/permission/PermissionTreeNodeVO.java
model/vo/role/RolePageItemVO.java
model/vo/role/RoleDetailVO.java
model/vo/internaluser/InternalUserPageItemVO.java
model/vo/internaluser/InternalUserDetailVO.java
```

其中：

- `PermissionTreeNodeVO` 用于前端树形展示
- `RoleDetailVO` 需要承载角色编辑页完整回显信息，可包含角色基础信息、统计信息、已分配权限和完整权限树
- `RolePageItemVO` 需要带 `permissionCount`、`userCount`
- 教务端账号相关 VO 建议回传 `dataScopeType` 与校区绑定信息

### 5.4 枚举要求

现有权限相关枚举已存在：

- `PermissionType`
- `PermissionStatus`

若后续需要更清晰的前端展示，可再新增：

- `RolePresetFlag`
- `RoleDeleteResultType`

里程碑 1 非必需。

## 6. DAO 层实现步骤

### 6.1 已有 DAO 基础

当前已存在：

- `PermissionDao`
- `RolePermissionDao`

### 6.2 需要补齐的 DAO Interface

建议新增：

```text
dao/RoleDao.java
dao/UserRoleDao.java
```

职责如下：

#### `RoleDao`

建议方法：

- `getById(Long roleId)`
- `getByCode(String roleCode)`
- `listByPage(RolePageQuery query)`
- `countByPage(RolePageQuery query)`
- `save(RoleEntity roleEntity)`
- `update(RoleEntity roleEntity)`
- `deleteById(Long roleId)`
- `countUserBinding(Long roleId)`

#### `UserRoleDao`

建议方法：

- `listByUserId(Long userId)`
- `listByRoleId(Long roleId)`
- `saveBatch(Long userId, List<Long> roleIds, Long operatorId, Long now)`
- `deleteByUserId(Long userId)`
- `deleteByRoleId(Long roleId)`
- `countByRoleId(Long roleId)`
- `listUserIdsByRoleId(Long roleId)`

### 6.3 Mapper 设计要求

必须遵守：

- 一张表尽量只对应一个 Mapper
- 参数非法值必须在 DAO 层提前拦截
- 不在 Mapper XML 中夹杂复杂业务规则

建议对应 Mapper：

```text
dao/mapper/RoleMapper.java
dao/mapper/UserRoleMapper.java
resources/mapper/role/RoleMapper.xml
resources/mapper/role/UserRoleMapper.xml
```

### 6.4 SQL 层要点

#### `sys_permission`

只做：

- 树查询
- 按角色查权限
- 按用户查权限

不做：

- 在线新增节点
- 在线改权限码
- 在线删节点

#### `sys_role_permission`

核心操作：

- 先删除角色旧权限
- 再批量插入新权限

该动作必须放到事务内。

#### `sys_role`

需支持：

- 角色列表分页
- 按 code 唯一校验
- 状态筛选
- 权限数量统计
- 用户数量统计

### 6.5 推荐 SQL 种子策略

权限树按“代码定义为准、数据库投影存储”的方式维护，建议：

- 代码中维护固定权限树常量或枚举定义
- `db/mysql/schema.sql` 只放表结构
- `db/h2/data.sql` 放测试数据或最小权限种子
- 生产/预发环境通过独立初始化 SQL 或版本迁移工具把代码权限树导入数据库

权限树字段建议至少包含：

- `id`
- `code`
- `name`
- `type`
- `parent_id`
- `path`
- `sort`
- `status`

## 7. Cache 层实现步骤

### 7.1 当前缓存能力

当前 `SecurityCacheService` 已有：

- 用户快照缓存
- 权限码缓存
- token active/revoked 标记

### 7.2 必须补齐的缓存接口

为了满足“权限修改后立即生效”，建议扩展：

```text
void clearAuthorityCodes(String userType, Long userId);
void clearUserSnapshot(String userType, Long userId);
void clearAuthorityCodesBatch(String userType, List<Long> userIds);
```

如需更完整，可再补：

```text
void clearAllAuthorityCodesByUserIds(String userType, List<Long> userIds);
```

### 7.3 缓存失效触发点

#### 角色权限保存后

需要：

- 找出该角色关联的所有教务端用户
- 清理这些用户的权限缓存

#### 用户角色调整后

需要：

- 清理目标用户权限缓存
- 清理目标用户快照缓存

#### 用户禁用/启用后

需要：

- 清理目标用户快照缓存
- 吊销该用户当前 token

### 7.4 Cache 层实现要求

- 缓存 key 需要包含 `userType + userId`
- 缓存 TTL 抽配置，不允许硬编码
- 清缓存方法命名使用 `clear...`
- 批量失效优先通过统一 key 前缀和批量删除实现

## 8. Security 层实现步骤

### 8.1 当前主链路

当前主链路已经存在：

```text
SecurityConfig
 -> JwtAuthenticationFilter
 -> MultiAccountUserDetailsService
 -> InternalAccountProvider / TeacherAccountProvider / StudentAccountProvider
 -> AuthorityCodeService
 -> PermissionDao
```

### 8.2 RBAC 模块需要补的安全能力

#### 1. 接口鉴权注解落地

所有教务端管理接口都必须先经过基础入口权限 `hasAuthority('admin:role')`。
该基础权限建议统一放在 `SecurityConfig` 的 `/admin/api/v1/**` 路由级规则中控制。
Controller 方法上的 `@PreAuthorize(...)` 继续负责各接口自己的细粒度权限判断。

角色与权限管理接口必须使用：

```java
@PreAuthorize("hasAuthority('admin:role:view')")
@PreAuthorize("hasAuthority('admin:role:create')")
@PreAuthorize("hasAuthority('admin:role:update')")
@PreAuthorize("hasAuthority('admin:role:delete')")
@PreAuthorize("hasAuthority('admin:role:assign_permission')")
```

教务端账号管理接口使用：

```java
@PreAuthorize("hasAuthority('admin:user:view')")
@PreAuthorize("hasAuthority('admin:user:create')")
@PreAuthorize("hasAuthority('admin:user:update')")
@PreAuthorize("hasAuthority('admin:user:enable')")
@PreAuthorize("hasAuthority('admin:user:reset_password')")
```

#### 2. 默认拒绝策略

`SecurityConfig` 需要保证：

- 登录、刷新、登出等公开接口显式放行
- 其余接口默认需要认证
- 敏感接口依赖 `@PreAuthorize` 精细控制

#### 3. 教师/学员端处理

根据需求文档：

- 教师端不做角色配置页面
- 教师端默认拥有教师端固定页面权限
- 学员端不做 RBAC 页面权限控制

因此 RBAC 管理接口只在教务端开放。

补充说明：

- 本章只描述认证主链路、接口鉴权和权限装载
- 数据范围类型、查询过滤和写操作范围校验统一放到 `第 12 章` 说明，避免与功能权限描述重复

### 8.3 用户详情装载要求

`InternalAccountProvider` 需要继续保持：

- 先读权限缓存
- 缓存未命中再回源数据库
- 权限列表为空时拒绝装载

后续如果支持用户多角色，可把教务端逻辑统一理解为：

```text
用户 -> 用户角色 -> 角色权限 -> 权限码
```

### 8.4 审计与日志要求

权限相关敏感操作需记录日志：

- 创建角色
- 编辑角色
- 删除角色
- 保存角色权限
- 创建教务端账号
- 修改教务端账号角色
- 禁用教务端账号
- 重置密码

禁止记录：

- 明文密码
- 完整 token

## 9. Utils 层实现步骤

### 9.1 建议新增工具或辅助组件

建议新增以下工具类或组装器：

```text
utils/tree/PermissionTreeBuilder.java
utils/time/TimeProvider.java
```

如不新建 `utils/tree` 包，也可放在 `service/permission/support` 中。

### 9.2 `PermissionTreeBuilder`

职责：

- 把 `List<PermissionEntity>` 组装成树
- 按 `parentId` 归并子节点
- 按 `sort` 排序
- 转成 `PermissionTreeNodeVO`

注意：

- 工具类只做纯结构组装
- 不要在工具类里访问 DAO

### 9.3 时间与 ID 处理

建议统一：

- `createdAt`
- `updatedAt`

都使用毫秒级 `Long`

如项目后续统一时间获取来源，建议抽象 `TimeProvider`，避免各 service 自己写 `System.currentTimeMillis()`

## 10. Service 层实现步骤

### 10.1 建议新增 Service Interface

```text
service/permission/PermissionTreeQueryService.java
service/role/RoleQueryService.java
service/role/RoleDetailQueryService.java
service/role/RoleCreateService.java
service/role/RoleUpdateService.java
service/role/RoleDeleteService.java
service/internaluser/InternalUserQueryService.java
service/internaluser/InternalUserCommandService.java
```

### 10.2 权限树查询服务

#### `PermissionTreeQueryService`

职责：

- 查询固定权限树
- 过滤禁用节点
- 组装树形返回

主要步骤：

1. 从 `PermissionDao` 查询教务端权限点
2. 过滤 `status != ENABLED` 的节点
3. 按 `sort` 排序
4. 通过 `PermissionTreeBuilder` 组装为树
5. 返回 `List<PermissionTreeNodeVO>`

### 10.3 角色查询服务

#### `RoleQueryService`

职责：

- 分页查角色
- 不承载角色详情查询

主要步骤：

1. 校验查询参数
2. 调用 `RoleDao` 查询角色列表
3. 调用 `RolePermissionDao` 统计权限数量
4. 调用 `UserRoleDao` 统计用户数量
5. 组装 `RolePageItemVO`

#### `RoleDetailQueryService`

职责：

- 查角色完整详情

查询角色详情步骤：

1. 校验 `RoleDetailQueryRequest`
2. 调用 `RoleDao` 查询角色基础信息及统计信息
3. 查询角色已分配 `permissionIds`
4. 查询完整权限树
5. 组装 `RoleDetailVO`

### 10.4 角色命令服务

#### `RoleCreateService`

职责：

- 创建角色

创建角色步骤：

1. 校验 `RoleCreateRequest`
2. 校验 `code` 唯一
3. 校验并去重 `permissionIds`
4. 保存角色基础信息
5. 保存角色权限关联
6. 返回新角色 `id`
7. 记录审计日志

#### `RoleUpdateService`

职责：

- 编辑角色

编辑角色步骤：

1. 校验入参
2. 校验角色存在
3. 只允许修改名称、描述、状态
4. 校验并去重 `permissionIds`
5. 更新角色基础信息
6. 覆盖更新角色权限
7. 清理相关用户权限缓存
8. 记录审计日志

#### `RoleDeleteService`

职责：

- 删除角色

删除角色步骤：

1. 校验 `roleId`
2. 校验角色存在
3. 读取 `RoleProperties.presetRoleCodes`
4. 基于角色 `code` 判断不是系统预置角色
5. 校验无用户绑定
6. 删除角色权限关联
7. 删除角色
8. 记录审计日志

系统预置角色规则：

- 当前通过配置项 `chordsked.role.preset-role-codes` 维护系统预置角色编码列表
- `RoleDeleteService` 会读取该配置，并用角色 `code` 做大小写无关匹配
- 默认值可配置为 `ADMIN`、`OPERATOR`
- 如果后续系统预置角色集合频繁变化，优先改配置项；如再进一步演进，可考虑新增数据库字段专门标记系统角色

### 10.5 角色权限处理说明

- 角色权限查询不再拆独立 service，统一由 `RoleDetailQueryService` 在详情查询时组装 `permissionIds` 和 `permissionTree`
- 角色权限保存不再拆独立接口，统一由 `RoleUpdateService` 在编辑角色时覆盖更新 `sys_role_permission`
- 如果后续业务再次出现“只刷新权限区域”或“只保存权限、不改角色基础信息”的场景，再考虑恢复独立权限 service

### 10.6 教务端账号查询服务

#### `InternalUserQueryService`

职责：

- 分页查询教务端账号
- 查询详情

查询列表步骤：

1. 校验分页参数
2. 按关键词/状态/角色/校区分页查询
3. 查询绑定角色和主校区
4. 组装返回

### 10.7 教务端账号命令服务

#### `InternalUserCommandService`

职责：

- 创建账号
- 编辑账号
- 启用/禁用
- 重置密码

创建账号步骤：

1. 校验用户名、手机号、角色、校区、`dataScopeType`
2. 校验用户名唯一、手机号唯一
3. 校验角色存在且启用
4. 生成初始密码并加密
5. 保存 `sys_internal_user`，写入账号级 `dataScopeType`
6. 保存 `sys_user_role`
7. 保存 `sys_user_campus`
8. 记录审计日志

编辑账号步骤：

1. 校验用户存在
2. 更新基础信息和 `dataScopeType`
3. 如角色调整，更新 `sys_user_role`
4. 如校区调整，更新 `sys_user_campus`
5. 清理用户权限缓存与快照缓存
6. 记录审计日志

启用/禁用步骤：

1. 校验目标用户存在
2. 校验不能禁用自己
3. 校验最后一个 ADMIN 保护逻辑
4. 更新用户状态
5. 清理用户快照缓存
6. 撤销已有 token
7. 记录审计日志

重置密码步骤：

1. 校验目标用户存在
2. 生成临时密码
3. 密码 BCrypt 加密
4. 标记首次登录强制修改密码
5. 清理登录态
6. 记录审计日志

## 11. Controller 层实现步骤

### 11.1 建议新增控制器

```text
controller/PermissionController.java
controller/RoleController.java
controller/InternalUserController.java
```

### 11.2 `PermissionController`

建议接口：

- `GET /admin/api/v1/permissions/tree`

职责：

- 参数校验
- 调用 `PermissionTreeQueryService`
- 返回统一 `ApiResponse`

### 11.3 `RoleController`

建议接口：

- `GET /admin/api/v1/roles`
- `GET /admin/api/v1/roles/{roleId}`
- `POST /admin/api/v1/roles`
- `PUT /admin/api/v1/roles/{roleId}`
- `DELETE /admin/api/v1/roles/{roleId}`

Controller 要点：

- 所有入参使用 `@Valid`
- 分页参数做边界校验
- 保持查询/编辑接口统一使用 `Request DTO + ResultVO` 风格
- `roleId` 使用 `@Min(1)`
- Swagger 注解补齐 `summary` 和 `description`
- 鉴权注解放在接口方法上

### 11.4 `InternalUserController`

建议接口：

- `GET /admin/api/v1/internal-users`
- `GET /admin/api/v1/internal-users/{userId}`
- `POST /admin/api/v1/internal-users`
- `PUT /admin/api/v1/internal-users/{userId}`
- `PUT /admin/api/v1/internal-users/{userId}/status`
- `PUT /admin/api/v1/internal-users/{userId}/reset-password`

Controller 只负责：

- 参数校验
- 提取当前登录人信息
- 调用 service
- 返回统一响应

## 12. 数据权限联动细则

### 12.1 功能权限

本章是在 `第 8 章 Security 层实现步骤` 基础上的补充说明，专门解释“功能权限校验”和“数据范围校验”如何协同工作。

RBAC 负责“能做什么”：

- 通过 `@PreAuthorize("hasAuthority('xxx')")` 进行校验
- 该校验仅判断当前登录用户是否具备调用接口或执行按钮动作的资格
- 该校验不替代数据范围校验

典型含义如下：

- `hasAuthority('admin:user:update')` 表示“可以执行修改账号这个动作”
- 但不表示“可以修改任意账号”
- 目标账号是否可被当前用户操作，还必须继续经过数据范围校验

### 12.2 数据权限

当前阶段数据权限不放在角色里，而放在账号侧：

- 教务端账号通过 `InternalUserEntity.dataScopeType` + 校区绑定共同控制数据范围
- 教师端账号通过校区 + 已分配学员控制数据范围

因此 RBAC 模块对数据权限的接口边界是：

- 角色接口不配置数据权限
- 用户接口配置校区和数据范围

当前阶段推荐的主数据来源如下：

- `InternalUserEntity.dataScopeType`：定义该教务账号的数据范围类型
- `UserCampusEntity`：定义该教务账号可作用的校区边界
- `DataScopeEntity`：保留为未来通用数据范围配置模型，不作为本期主判断来源

也就是说，里程碑 1 中“角色决定功能权限”“用户决定数据范围”是明确分工，不采用“角色顺带决定数据范围”的主方案。

这里的重点不是重新定义接口，而是明确：

- `第 4 章` 中的接口清单只定义“接口存在与鉴权入口”
- 真正的数据过滤与目标对象校验逻辑，以本章为准

### 12.3 数据范围类型与当前阶段口径

当前阶段建议统一数据范围类型语义如下：

- `ALL`：允许访问全量数据
- `CAMPUS`：仅允许访问当前账号绑定校区范围内的数据
- `SELF`：仅允许访问当前登录人本人相关的数据
- `ASSIGNED`：预留语义，表示“仅允许访问分配给当前用户负责的数据”

与当前代码模型的对应关系建议统一为：

- `ALL` 对应 `InternalUserDataScopeType.ALL_COMPANY`
- `SELF` 对应 `InternalUserDataScopeType.SELF_ONLY`
- `CAMPUS` 建议作为当前实现目标语义，与现有枚举中的“指定校区”口径统一处理
- 现有 `InternalUserDataScopeType` 若命名仍偏旧，可在后续重构时再统一命名，但本期接口语义应先保持一致

当前阶段落地建议：

- 教务端账号优先实现 `ALL`、`CAMPUS`、`SELF`
- `ASSIGNED` 不在本期 RBAC 文档中写死具体表结构
- 当未来确实需要支持“某教务只负责部分账号/学员/节点”时，再补独立的数据分配模型设计

### 12.4 数据范围校验责任划分

数据范围校验不能只依赖 `@PreAuthorize`，必须分查询和写操作两类处理。

#### 1. 查询类接口

例如：

- 列表查询
- 详情查询
- 分页筛选

建议通过以下方式处理：

- Mapper 方法标注 `@DataScope`
- 由 `DataScopeInterceptor` 根据当前用户的 `dataScopeType` 自动改写 SQL
- 对应策略类负责拼接 `WHERE` 条件

查询类接口推荐链路：

```text
Controller
 -> @PreAuthorize 校验功能权限
 -> Service 调用 DAO
 -> Mapper 方法带 @DataScope
 -> DataScopeInterceptor 注入过滤条件
 -> 数据库返回已过滤结果
```

#### 2. 写操作接口

例如：

- 修改账号
- 删除角色绑定对象
- 重置密码
- 禁用账号

写操作通常不能只靠 SQL 自动拼接解决，因为要操作的目标对象通常是单个资源，必须先校验目标资源是否在数据范围内。

建议通过 Service 层显式校验：

- `validateCanReadTarget(...)`
- `validateCanUpdateTarget(...)`
- `validateCanDeleteTarget(...)`

写操作推荐链路：

```text
Controller
 -> @PreAuthorize 校验功能权限
 -> Service 读取目标对象
 -> Service 执行数据范围校验
 -> 校验通过后再更新或删除
```

### 12.5 教务端账号相关接口的数据范围校验建议

以教务端账号管理为例，建议按如下方式实现：

#### 1. 查询账号列表

- 先校验 `admin:user:view`
- 再按 `dataScopeType` 过滤结果
- `ALL`：不过滤
- `CAMPUS`：只返回绑定校区内账号
- `SELF`：只返回当前登录账号本人
- `ASSIGNED`：本期不落地，保留为后续扩展点

#### 2. 修改账号

- 先校验 `admin:user:update`
- 再判断目标账号是否在当前登录人的数据范围内
- 不在范围内则拒绝操作

#### 3. 启用/禁用账号

- 先校验 `admin:user:enable`
- 再判断目标账号是否在当前登录人的数据范围内
- 再做“不能禁用自己”“最后一个 ADMIN 不可禁用”等业务校验

#### 4. 重置密码

- 先校验 `admin:user:reset_password`
- 再判断目标账号是否在当前登录人的数据范围内
- 校验通过后执行密码重置和登录态清理

### 12.6 `CAMPUS` 与 `ASSIGNED` 的边界说明

当前文档语义中：

- `CAMPUS` 表示“按校区边界管理数据”
- `ASSIGNED` 表示“按明确分配关系管理数据”

若系统当前并未建立“哪个教务负责哪个具体对象”的明确业务规则，则：

- 不应把 `ASSIGNED` 和 `CAMPUS` 混为一谈
- 更不应把 `ASSIGNED` 默认解释为“该教务负责所属校区全部数据”
- 这种场景应直接使用 `CAMPUS`

因此本期建议：

- 前端若保留 `ASSIGNED` 选项，只作为预留态，不建议实际开放使用
- 后端文档不写死 `ASSIGNED` 的具体表结构实现
- 需要落地 `ASSIGNED` 时，再补专项设计文档定义“分配对象”“分配关系来源”“读写校验方式”

### 12.7 与 MyBatis DataScope 的配合

根据架构设计文档，后续需补：

- `@DataScope` 注解
- `DataScopeInterceptor`
- 各类 `DataScopeStrategy`

这部分不是 RBAC 主接口的第一优先级，但接口设计要为它留接口数据来源：

- 当前用户类型
- 当前用户 ID
- 当前用户主校区
- 绑定校区范围
- 数据范围类型

建议补充的上下文信息：

- 当前用户角色代码集合
- 当前请求目标资源主键
- 当前请求是否为查询类还是写操作
- 当前 Mapper 目标表别名

补充说明：

- 若未来需要真正落地 `ASSIGNED`、按资源负责人分配、按主体覆盖默认范围等能力，可优先复用 `DataScopeEntity`
- 在这些能力真正进入需求前，不建议删除 `DataScopeEntity`，但也不建议让其与 `InternalUserEntity.dataScopeType` 同时承担同一条主业务链路的当前口径

## 13. SQL 与初始化步骤

### 13.1 权限树初始化

需要根据 `权限点清单.md` 生成一份完整的 `sys_permission` 种子数据，至少覆盖：

- 认证模块
- 用户管理模块
- 角色管理模块
- 校区管理模块
- 审计日志模块

由于原始 `权限点清单.md` 中存在 `menu:user`、`user:view` 这一类旧格式权限码，而本期 RBAC 文档已统一要求使用带端前缀的权限码，因此在落库前必须先做一次权限码适配。

#### 13.1.1 教务端权限码适配清单

适配原则：

- 菜单权限统一改为：`admin:{resource}:menu`
- 按钮权限统一改为：`admin:{resource}:{action}`
- 里程碑 1 中教务端 RBAC 只维护教务端权限树，因此本节清单默认全部挂在 `admin` 端下

建议按以下口径整理并落库：

| 原权限码 | 新权限码 | 权限名称 | 类型 |
|---------|---------|---------|------|
| `menu:auth` | `admin:auth:menu` | 认证管理菜单 | 菜单 |
| `auth:logout` | `admin:auth:logout` | 强制登出 | 按钮 |
| `menu:user` | `admin:user:menu` | 用户管理菜单 | 菜单 |
| `user:view` | `admin:user:view` | 查看用户 | 按钮 |
| `user:create` | `admin:user:create` | 创建用户 | 按钮 |
| `user:update` | `admin:user:update` | 编辑用户 | 按钮 |
| `user:delete` | `admin:user:delete` | 删除用户 | 按钮 |
| `user:enable` | `admin:user:enable` | 启用/禁用用户 | 按钮 |
| `user:reset_password` | `admin:user:reset_password` | 密码重置 | 按钮 |
| `menu:role` | `admin:role:menu` | 角色管理菜单 | 菜单 |
| `role:view` | `admin:role:view` | 查看角色 | 按钮 |
| `role:create` | `admin:role:create` | 创建角色 | 按钮 |
| `role:update` | `admin:role:update` | 编辑角色 | 按钮 |
| `role:delete` | `admin:role:delete` | 删除角色 | 按钮 |
| `role:assign_permission` | `admin:role:assign_permission` | 分配权限 | 按钮 |
| `menu:campus` | `admin:campus:menu` | 校区管理菜单 | 菜单 |
| `campus:view` | `admin:campus:view` | 查看校区 | 按钮 |
| `campus:create` | `admin:campus:create` | 创建校区 | 按钮 |
| `campus:update` | `admin:campus:update` | 编辑校区 | 按钮 |
| `campus:delete` | `admin:campus:delete` | 删除校区 | 按钮 |
| `campus:assign_user` | `admin:campus:assign_user` | 分配用户 | 按钮 |
| `menu:log` | `admin:log:menu` | 审计日志菜单 | 菜单 |
| `log:view` | `admin:log:view` | 查看日志列表 | 按钮 |
| `log:detail` | `admin:log:detail` | 查看日志详情 | 按钮 |
| `log:export` | `admin:log:export` | 导出日志 | 按钮 |
| `log:sensitive` | `admin:log:sensitive` | 查看敏感日志 | 按钮 |

#### 13.1.2 建议落库的权限树结构

在 `sys_permission` 中建议采用“根节点 + 菜单节点 + 按钮节点”的固定树结构：

```text
系统权限根节点
 -> 认证管理(admin:auth:menu)
    -> 强制登出(admin:auth:logout)
 -> 用户管理(admin:user:menu)
    -> 查看用户(admin:user:view)
    -> 创建用户(admin:user:create)
    -> 编辑用户(admin:user:update)
    -> 删除用户(admin:user:delete)
    -> 启用/禁用用户(admin:user:enable)
    -> 密码重置(admin:user:reset_password)
 -> 角色管理(admin:role:menu)
    -> 查看角色(admin:role:view)
    -> 创建角色(admin:role:create)
    -> 编辑角色(admin:role:update)
    -> 删除角色(admin:role:delete)
    -> 分配权限(admin:role:assign_permission)
 -> 校区管理(admin:campus:menu)
    -> 查看校区(admin:campus:view)
    -> 创建校区(admin:campus:create)
    -> 编辑校区(admin:campus:update)
    -> 删除校区(admin:campus:delete)
    -> 分配用户(admin:campus:assign_user)
 -> 审计日志(admin:log:menu)
    -> 查看日志列表(admin:log:view)
    -> 查看日志详情(admin:log:detail)
    -> 导出日志(admin:log:export)
    -> 查看敏感日志(admin:log:sensitive)
```

推荐结构：

```text
系统权限根节点
   -> 按钮节点
```

### 13.2 预置角色初始化

至少初始化：

- `ADMIN`
- `OPERATOR`

并初始化默认角色权限：

- `ADMIN` 拥有全部权限
- `OPERATOR` 按文档绑定只读或部分权限

### 13.3 约束与索引

建议确保：

- `sys_role.code` 唯一
- `sys_permission.code` 唯一
- `sys_role_permission(role_id, permission_id)` 唯一
- `sys_user_role(user_id, role_id)` 唯一

## 14. 实施顺序

本章为最终开发推进顺序。

前文 `第 5-12 章` 按技术分层描述“每层该做什么”，本章按项目推进顺序描述“开发时先做什么、后做什么”。

### 阶段 1：固定权限树落库

1. 完整整理权限点清单
2. 生成 `sys_permission` 初始化 SQL
3. 初始化 `ADMIN`、`OPERATOR` 角色及默认权限
4. 验证 `AuthorityCodeService` 能正确装载

### 阶段 2：权限树查询与角色权限回显

1. 补 `PermissionTreeQueryService`
2. 实现 `GET /admin/api/v1/permissions/tree`
3. 实现 `GET /admin/api/v1/roles/{roleId}`，将角色基础信息与权限回显合并
4. 补树组装 VO 与角色详情 VO

### 阶段 3：角色 CRUD 与权限保存

1. 补 `RoleDao`、`UserRoleDao`
2. 实现角色分页、详情、创建、编辑、删除
3. 实现 `PUT /admin/api/v1/roles/{roleId}`，同时保存角色基础信息与权限配置
4. 加事务和审计日志

### 阶段 4：教务端账号角色绑定

1. 实现教务端账号列表
2. 实现创建、编辑、状态变更、密码重置
3. 打通角色绑定与校区绑定
4. 清理权限缓存与登录态

### 阶段 5：缓存与安全收口

1. 补齐权限缓存失效方法
2. 完成角色变更即时生效
3. 完成 token 吊销联动
4. 增加审计日志

### 阶段 6：测试与验收

1. 单测
2. 集成测试
3. H2 初始化验证
4. 权限回归验证

## 15. 测试清单

### 15.1 DAO 层

- 角色按 code 唯一查询
- 角色分页查询
- 角色权限批量替换
- 用户角色绑定查询
- 用户角色批量替换

### 15.2 Service 层

- 创建角色成功
- 创建角色 code 冲突
- 删除系统预置角色失败
- 删除有关联用户的角色失败
- 保存角色权限后缓存被清理
- 编辑教务端账号后缓存被清理
- 禁用账号后 token 被撤销

### 15.3 Controller 层

- 参数缺失校验
- 非法分页参数校验
- 非法 `roleId`、`userId` 校验
- 无权限访问返回 403

### 15.4 Security 层

- `ADMIN` 可访问角色管理接口
- `OPERATOR` 无法访问角色编辑接口
- 未登录访问返回未认证
- 权限保存后重新访问接口立即生效

### 15.5 集成测试

至少补充：

- 角色创建 -> 绑定权限 -> 用户绑定角色 -> 登录 -> 访问受控接口成功
- 删除角色权限 -> 清缓存 -> 同一用户访问受控接口失败

## 16. 验收标准

当以下条件全部满足时，可认为 RBAC 模块达到里程碑 1 可交付状态：

- 教务端角色可以分页查询、创建、编辑、删除
- 固定权限树可以查询并正确展示层级
- 角色权限可以回显并保存
- 教务端账号可以绑定角色与校区
- 登录后能装载数据库中的权限码
- `@PreAuthorize` 权限校验生效
- 权限修改后无需重新登录即可生效
- 角色接口与账号接口都有审计日志
- 单测和集成测试覆盖关键场景

## 17. 不在本期范围内

以下能力不属于里程碑 1 RBAC 模块范围：

- 权限节点在线新增
- 权限节点在线编辑
- 权限节点在线删除
- 权限树拖拽排序后台维护
- 家长端权限系统
- 二期角色如 `CAMPUS_ADMIN`、`HQ_ADMIN` 的完整交付
- 基于属性的 ABAC 复杂授权

## 18. 开发时的最终检查清单

提交前逐项确认：

- 路由是否统一为 `admin/api/v1/...`
- Controller/Service/DAO 是否都做了参数校验
- Service 是否只依赖 DAO Interface
- 敏感操作是否加事务
- 角色权限保存后是否清理缓存
- 禁用账号后是否清理快照与 token
- 是否没有记录明文密码和完整 token
- 权限码命名是否统一
- 时间字段是否统一 `Long/BIGINT`
- Swagger 注解是否完整
- 是否补齐单测与集成测试
