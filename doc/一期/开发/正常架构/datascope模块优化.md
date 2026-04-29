# datascope 模块优化决策

## 1. 当前问题

你现在提出的核心问题其实有两个：

- `DataScopeInterceptor` 作为框架层组件，当前方法拆分偏细，阅读体验一般
- `DataScope` 既然准备作为统一的数据隔离能力，是否还应该继续绑定 `InternalUserDataScopeType` 以及一批 `internal` 语义服务

同时，你还关心一个很实际的问题：

- 当前 `internaluser`、`permission`、`role` 这些服务本身就是教务端专用能力，是否值得为了 `DataScope` 通用化而大改这些业务服务

这份文档给出的结论是：**不要大改 internal 业务服务本身，优先只改 DataScope 框架层，使其具备三端复用能力；internal 业务服务仍然可以保持现有命名和职责。**

## 2. 结论

### 2.1 internal 业务服务本身不需要为了 DataScope 通用化而大改

以下代码目前都属于“教务端 RBAC 管理域”：

- `backend/src/main/java/com/chordsked/backend/service/internaluser/impl/InternalUserQueryServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/permission/impl/InternalPermissionTreeQueryServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/impl/RoleQueryServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/impl/RoleCreateServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/impl/RoleDeleteServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/impl/RoleUpdateServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/impl/RoleDetailQueryServiceImpl.java`

这些服务的业务语义本身就是：

- 教务端账号管理
- 教务端权限树管理
- 教务端角色管理

因此：

- 它们保留 `Internal...` / `Role...` 命名是合理的
- 它们继续只服务 `ADMIN` 场景也是合理的
- 不需要为了 DataScope 扩展教师端、学生端，就把这些 Service 全部抽象成通用 `User...` / `Permission...` / `Role...` 服务

一句话：

- **业务服务可以保持 internal 专用**
- **真正需要通用化的是 DataScope 框架层**

### 2.2 应该修改的是 DataScope 层，而不是 internal 服务层

当前 DataScope 层已经是基础设施雏形：

- `backend/src/main/java/com/chordsked/backend/datascope/annotation/DataScope.java`
- `backend/src/main/java/com/chordsked/backend/datascope/context/DataScopeUserContext.java`
- `backend/src/main/java/com/chordsked/backend/datascope/interceptor/DataScopeInterceptor.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/`

如果后续希望：

- 教师端只看本校区课程安排
- 教师端只看自己负责的排课/学员
- 学员端只看自己的数据
- 不同端都能复用同一条数据隔离链路

那么正确改法应是：

- 让 `DataScope` 层不再依赖 `InternalUserDataScopeType`
- 让系统中的数据范围语义统一收敛为 `UserDataScopeType`
- 让 `DataScope` 层不假设“只有 ADMIN 才会进入该链路”
- 让统一用户上下文以 `UserDataScopeType` 为唯一数据范围表达
- 但不要求每一种用户实体都必须持久化 `dataScopeType` 字段

## 3. 建议的总策略

建议采用下面这个原则：

### 原则一：业务域保持专用

- `InternalUserQueryServiceImpl` 继续是教务端账号查询服务
- `InternalPermissionTreeQueryServiceImpl` 继续是教务端权限树查询服务
- `RoleQueryServiceImpl` 等继续是教务端角色管理服务

不建议改成：

- `UserQueryServiceImpl`
- `PermissionTreeQueryServiceImpl`
- `CommonRoleQueryServiceImpl`

因为这会让业务边界变模糊，而且当前项目并没有教师端、学员端通用角色管理场景。

### 原则二：框架层做通用化

应重点修改：

- `DataScopeInterceptor`
- `DataScopeUserContext`
- `DataScopeStrategy`
- 数据范围枚举

让它们成为真正的：

- 三端可复用
- 与具体业务域解耦
- 与具体表结构弱耦合

补充约束：

- DataScope 新增组件的注入优先使用 `@Resource(name = "...")`
- 关键异常点必须记录日志，但不得输出 token、密码、敏感快照内容

### 原则三：统一的是“数据范围语义”，不是“每张用户表都必须长同样字段”

这里要特别区分两件事：

- **框架层统一**
  - 统一使用 `UserDataScopeType`
  - 统一使用 `DataScopeInterceptor`
  - 统一使用 `DataScopeUserContext`
- **业务数据来源可以不同**
  - 教务端：数据范围可以来自 `sys_internal_user.data_scope_type`
  - 教师端：数据范围可以由业务规则直接确定，例如默认 `CAMPUS`
  - 学员端：数据范围也可以由业务规则直接确定，例如默认 `SELF` 或 `CAMPUS`

因此：

- 不需要为了 DataScope 通用化，就强行给 `TeacherUserEntity`、`StudentUserEntity` 增加 `dataScopeType`
- 更合理的做法是：**统一框架表达，允许不同端以不同方式提供该表达**

### 原则四：先统一枚举，再统一链路，再逐步迁移场景

不要先去全量改业务服务，而应按以下顺序推进：

1. 新增统一数据范围枚举 `UserDataScopeType`
2. 全量替换 `InternalUserDataScopeType`
3. 调整 `DataScopeUserContext`
4. 调整 `DataScopeInterceptor`
5. 保持 `internal-users` 先继续可用
6. 后续新增教师端/学生端数据隔离查询时直接接入

## 4. 哪些代码不建议大改

### 4.1 `InternalUserQueryServiceImpl`

当前职责：

- 校验教务端账号列表查询参数
- 查询教务端账号列表
- 聚合角色 ID

这个服务本身是业务服务，不是框架服务。

因此建议：

- 保留 `InternalUserQueryServiceImpl`
- 保留其 `internaluser` 包路径
- 保留 `PageResult<InternalUserQueryResultVO>` 返回结构

只需要确保：

- 它不再手工做数据范围判断
- 继续依赖 `DataScope` 自动注入

### 4.2 `InternalPermissionTreeQueryServiceImpl`

当前职责：

- 查询教务端权限树
- 构建 ADMIN 端权限树

这个服务本身不是数据范围隔离场景，而是权限树查询。

因此建议：

- 暂时完全不动
- 它不属于本轮 DataScope 优化重点

原因：

- 它主要解决的是“功能权限树展示”
- 不是“数据行级过滤”

### 4.3 `service/role/`

当前 `role` 目录下的服务也是教务端 RBAC 域：

- 角色列表
- 角色详情
- 创建角色
- 编辑角色
- 删除角色

这部分建议：

- 暂时保持不动
- 后续如果某个角色相关查询需要数据隔离，再让对应 Mapper 接 `@DataScope`

而不是先为了“可能以后会通用”就去重构整套角色服务。

## 5. 哪些代码建议修改

## 5.1 新增统一枚举 `UserDataScopeType`

建议新增：

- `backend/src/main/java/com/chordsked/backend/model/enums/UserDataScopeType.java`

建议最小枚举值：

- `ALL`
- `CAMPUS`
- `SELF`

可预留：

- `DEPARTMENT`
- `ASSIGNED`

说明：

- 这是框架层通用枚举
- 只表达“数据范围语义”
- 不直接绑定某个端的数据库字段命名

### 5.2 全量替换 `InternalUserDataScopeType`

本轮不再采用“双枚举映射”方案，而是直接把系统中的数据范围类型统一为：

- `UserDataScopeType`

原因：

- 两套近义枚举长期并存，后续维护容易混乱
- 新增范围类型时，容易出现一边修改、一边遗漏
- 缓存、`UserDetails`、Entity、VO、测试会长期处于双维护状态
- `DataScope` 既然是统一基础设施，就不应继续挂靠在 internal 专用枚举上

因此建议：

- 直接删除 `InternalUserDataScopeType`
- 由 `UserDataScopeType` 成为系统唯一的数据范围枚举
- 教务端、教师端、学生端后续都统一使用该枚举

兼容策略：

- 尽量保持原数据库 code 不变
- 只统一代码层枚举名称与语义

建议映射为：

- `ALL_COMPANY` 语义收敛为 `ALL`
- `SPECIFIED_CAMPUS` 语义收敛为 `CAMPUS`
- `SELF_ONLY` 语义收敛为 `SELF`
- 如果原先存在 `DEPARTMENT_AND_SUBORDINATE`，则统一为 `DEPARTMENT`

### 5.2.1 替换边界说明

这里的“全量替换”指的是：

- 代码层不再保留 `InternalUserDataScopeType`
- 统一改为 `UserDataScopeType`

但不表示：

- `TeacherUserEntity` 必须新增 `dataScopeType`
- `StudentUserEntity` 必须新增 `dataScopeType`

更准确地说：

- **统一替换的是“类型体系”**
- **不是强制所有用户表结构都引入同一个字段**

### 5.2.2 教务端、教师端、学员端的建议数据来源

建议按下面方式理解：

- `InternalUserEntity`
  - 保留 `data_scope_type` 字段
  - 只是字段类型由 `InternalUserDataScopeType` 改成 `UserDataScopeType`
  - 因为教务端本身就有更灵活的数据范围配置需求

- `TeacherUserEntity`
  - 当前不建议新增 `dataScopeType`
  - 先按业务规则处理，例如默认 `CAMPUS`
  - 如果未来教师端出现“教师管理员可看全部、普通教师只能看本校区”等差异，再决定是否补字段

- `StudentUserEntity`
  - 当前不建议新增 `dataScopeType`
  - 先按业务规则处理，例如默认 `SELF`，某些场景可由 Service 或上下文映射为 `CAMPUS`
  - 如果未来学生端真出现多种可配置数据范围，再考虑是否补字段

一句话：

- **教务端可以持久化数据范围**
- **教师端、学员端当前可以按规则推导数据范围**

### 5.3 调整 `ChordSkedUserDetails`

本轮建议直接修改 `ChordSkedUserDetails` 的数据范围字段类型：

- 从 `InternalUserDataScopeType`
- 改为 `UserDataScopeType`

这样做的目的：

- 让认证链路与 DataScope 链路使用同一套数据范围模型
- 避免再额外提供“兼容 getter”
- 避免后续教师端、学生端接入时继续扩散 internal 专用语义

同步影响：

- 登录装载逻辑要改
- 安全快照缓存读写要改
- 相关单元测试和集成测试要改

补充说明：

- `ChordSkedUserDetails` 中统一持有 `UserDataScopeType`
- 但该值的来源不必完全一致
- 教务端可以来自数据库字段
- 教师端、学生端可以来自固定规则或后续扩展逻辑

### 5.4 调整 `DataScopeUserContext`

当前 `DataScopeUserContext` 不应再依赖 `InternalUserDataScopeType`。

建议改为：

- `userId`
- `userType`
- `UserDataScopeType dataScopeType`
- `List<Long> campusIds`

这样：

- 教务端可以用
- 教师端可以用
- 学员端如果后续需要，也可以用

补充说明：

- `DataScopeUserContext` 代表的是“进入 DataScope 框架层后”的统一上下文
- 它不关心这个 `dataScopeType` 是来自数据库字段，还是来自业务规则推导

### 5.5 调整 `DataScopeInterceptor`

这是本轮优化重点。

建议改造目标：

- 方法数量适度收敛，提升可读性
- 取消对 `InternalUserDataScopeType` 的直接依赖
- 取消“只有 ADMIN 才能走 DataScope”这一硬编码假设
- 明确关键异常点的日志策略

#### 结构收敛建议

当前可收敛为 6 个核心方法：

1. `intercept(...)`
2. `getMappedStatement(...)`
3. `getDataScopeAnnotation(...)`
4. `buildUserContext(...)`
5. `buildScopeCondition(...)`
6. `appendCondition(...)`

具体建议：

- 合并 `findDataScope()` + `resolveDataScope()` 为 `getDataScopeAnnotation()`
- 合并 `resolveCondition()` + `resolveStrategy()` 为 `buildScopeCondition()`
- 合并 `findInsertIndex()` + `firstMatchStart()` + `containsWhere()` 到 `appendCondition()`

这样做之后：

- 保持代码仍有清晰分段
- 又不会因为方法过多导致阅读跳转频繁

#### 用户类型支持建议

当前代码中这段判断：

```java
if (!AccountUserType.ADMIN.equals(userDetails.getUserTypeEnum())) {
    throw new IllegalStateException("current user type is unsupported");
}
```

不建议长期保留。

建议修改为：

- 不直接限制必须为 `ADMIN`
- 只要求当前 `UserDetails` 能提供：
  - `userId`
  - `userType`
  - `UserDataScopeType`

然后在具体策略中决定是否能处理。

更准确地说：

- 框架层不再限制“谁能走”
- 业务是否接入 DataScope，由 Mapper 的 `@DataScope` 决定

进一步说明：

- 对教务端用户，`dataScopeType` 可以来自用户表字段
- 对教师端用户，`dataScopeType` 可以先固定映射为 `CAMPUS`
- 对学员端用户，`dataScopeType` 可以先固定映射为 `SELF`
- 后续如果某一端出现更复杂规则，再单独扩展，不影响 `DataScope` 框架结构

### 5.6 调整策略实现

当前：

- `AllDataScopeStrategy`
- `CampusDataScopeStrategy`
- `SelfDataScopeStrategy`

这三个策略本身可以保留。

但要统一依赖：

- `UserDataScopeType`
- `DataScopeUserContext`

不要继续依赖：

- `InternalUserDataScopeType`
- 只面向 ADMIN 的上下文假设

### 5.7 校区列表加载策略

当前 `DataScopeInterceptor` 中：

```java
List<Long> campusIds = dataScopeType.isCampusScope()
        ? userCampusDaoProvider.getObject().listCampusIdsByUserId(userDetails.getUserId())
        : List.of();
```

这在第一版可以接受，但后续建议优化。

建议分两步：

第一阶段：

- 继续保留当前实现
- 只改通用数据范围枚举与上下文

第二阶段：

- 增加请求级缓存，避免同一请求多次查同一个用户的校区列表

当前这不是必须项，不建议和本轮结构优化同时做。

## 6. 推荐的具体改造方案

建议按下面顺序改。

### 第一步：新增 `UserDataScopeType`

新增通用枚举，并补基础方法：

- `isAllScope()`
- `isCampusScope()`
- `isSelfScope()`

### 第二步：全局替换 `InternalUserDataScopeType`

建议修改范围至少包括：

- `InternalUserEntity`
- `ChordSkedUserDetails`
- `SecurityCacheService.SecurityUserSnapshot`
- `InternalAccountProvider`
- 相关 DTO / VO
- `DataScopeUserContext`
- `DataScopeInterceptor`
- `datascope/strategy/`
- 所有相关测试

要求：

- 删除旧枚举文件
- 全局引用统一切换到 `UserDataScopeType`
- 保持原 code 兼容数据库现有存量数据

### 第三步：调整 `ChordSkedUserDetails`

直接把字段与 getter/setter 改为：

```java
private UserDataScopeType dataScopeType;
```

并保留常用判断方法依赖统一枚举语义。

### 第四步：明确不同端的数据范围来源

建议按端区分来源：

- 教务端：从 `InternalUserEntity.dataScopeType` 读取
- 教师端：当前按业务规则写死为 `UserDataScopeType.CAMPUS`
- 学员端：当前按业务规则写死为 `UserDataScopeType.SELF`

这样做的好处：

- 不破坏当前教师端、学员端表结构
- 先让三端都能进入统一的 DataScope 框架
- 未来如果教师端、学员端出现复杂范围配置，再决定是否持久化字段

### 第五步：调整 `DataScopeUserContext`

把其中的数据范围类型改成：

- `UserDataScopeType`

### 第六步：重构 `DataScopeInterceptor`

建议同步做两件事：

- 结构收敛
- 类型通用化

目标效果：

- 代码更短
- 可读性更强
- 不再依赖 `InternalUserDataScopeType`
- 不再写死 `ADMIN`

### 第七步：调整缓存与登录装载链路

重点修改：

- `SecurityCacheService`
- `SecurityCacheServiceImpl`
- `InternalAccountProvider`
- 相关快照测试

要求：

- 安全快照中的数据范围字段统一使用 `UserDataScopeType`
- 旧数据如有兼容需求，需要在解析逻辑中按 code 做兼容
- 新写入一律采用统一枚举语义

其中登录装载建议：

- `InternalAccountProvider`：从数据库读取 `UserDataScopeType`
- `TeacherAccountProvider`：按当前规则写入 `UserDataScopeType.CAMPUS`
- `StudentAccountProvider`：按当前规则写入 `UserDataScopeType.SELF`

### 第八步：保留现有 internal 业务服务不动

这一步非常重要。

不要同时去改：

- `InternalUserQueryServiceImpl`
- `InternalPermissionTreeQueryServiceImpl`
- `service/role/`

除非它们本身存在明确 bug 或后续新增查询场景要接 `@DataScope`。

## 7. 建议的边界

### 这轮建议修改

- `backend/src/main/java/com/chordsked/backend/datascope/interceptor/DataScopeInterceptor.java`
- `backend/src/main/java/com/chordsked/backend/datascope/context/DataScopeUserContext.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/`
- `backend/src/main/java/com/chordsked/backend/model/enums/`
- `backend/src/main/java/com/chordsked/backend/security/account/model/ChordSkedUserDetails.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/`
- `backend/src/main/java/com/chordsked/backend/cache/security/`
- `backend/src/main/java/com/chordsked/backend/model/entity/InternalUserEntity.java`
- 相关测试代码

### 这轮建议不动

- `backend/src/main/java/com/chordsked/backend/service/internaluser/impl/InternalUserQueryServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/permission/impl/InternalPermissionTreeQueryServiceImpl.java`
- `backend/src/main/java/com/chordsked/backend/service/role/`

## 8. 实施主顺序

以下是最终执行时应采用的**唯一主顺序**，后文不再单独定义另一套并行顺序。

1. 新增 `UserDataScopeType`
2. 替换 `InternalUserEntity`
3. 替换 `ChordSkedUserDetails`
4. 替换安全快照与缓存实现
5. 替换三个 AccountProvider
6. 替换 `DataScopeUserContext`
7. 替换策略类
8. 重构 `DataScopeInterceptor`
9. 清理 DTO / VO / 其他残留引用
10. 删除 `InternalUserDataScopeType`
11. 跑相关单元测试与集成测试
12. 更新文档与收尾

## 9. 最终决策

最终建议如下：

1. `internaluser`、`permission`、`role` 这些教务端专用业务服务不需要为 DataScope 通用化而大改
2. 真正应该修改的是 `DataScope` 基础设施层
3. `InternalUserDataScopeType` 不再保留，改为由 `UserDataScopeType` 统一替换
4. `UserDataScopeType` 成为系统唯一的数据范围枚举
5. 统一的是“数据范围类型体系”和“框架层上下文”，不是要求所有用户实体都新增 `dataScopeType`
6. `TeacherUserEntity`、`StudentUserEntity` 当前不建议新增 `dataScopeType` 字段，先按业务规则推导
7. `DataScopeInterceptor` 应适度合并方法，减少阅读跳转，但不要把所有逻辑压进一个超长方法
8. 当前阶段先完成“统一枚举 + DataScope 框架层通用化”，后续老师端、学生端场景直接复用这条链路

## 10. 一句话方案

一句话总结就是：

- **internal 业务服务保持专用，不大改**
- **DataScope 框架层做通用化升级**
- **通过 `UserDataScopeType` 全量替换 `InternalUserDataScopeType`，但不强制教师端、学员端实体新增 `dataScopeType` 字段**
- **让三端以“统一框架表达 + 各自数据来源”的方式接入同一条数据隔离链路**

## 11. 最终替换实施顺序清单

这部分给出一份可直接执行的落地顺序。

原则：

- 一次只处理一层
- 每一步都保证可编译、可测试
- 先统一类型体系，再统一认证上下文，再统一 DataScope 框架层
- 不在同一轮同时大改业务 Service
- 本节是对“实施主顺序”的展开说明，不再定义第二套独立顺序

### 阶段 0：替换前确认

目标：

- 先确认当前系统里所有 `InternalUserDataScopeType` 的使用位置
- 明确哪些地方是真正要替换，哪些地方当前不动

建议操作：

1. 全局搜索 `InternalUserDataScopeType`
2. 按使用场景分类：
   - 实体层
   - `UserDetails`
   - 安全缓存快照
   - 登录装载
   - DataScope 框架层
   - DTO / VO
   - 单元测试 / 集成测试
3. 建一个临时替换 checklist，逐项勾掉

完成标准：

- 所有受影响文件范围明确
- 确认教师端、学员端当前不新增数据库字段

### 阶段 1：新增统一枚举 `UserDataScopeType`

目标：

- 先把统一类型定义出来，作为后续唯一目标类型

建议新增文件：

- `backend/src/main/java/com/chordsked/backend/model/enums/UserDataScopeType.java`

建议内容：

- 枚举值：
  - `ALL`
  - `DEPARTMENT`
  - `SELF`
  - `CAMPUS`
- 基础方法：
  - `getCode()`
  - `fromCode(Integer code)`
  - `isAllScope()`
  - `isDepartmentScope()`
  - `isSelfScope()`
  - `isCampusScope()`

注意：

- 尽量复用当前数据库中已存在的 code
- 只统一代码语义，不先改数据库存量值

完成标准：

- `UserDataScopeType` 可单独编译通过
- code 与现有数据库值兼容

### 阶段 2：实体层替换

目标：

- 把教务端用户实体中的数据范围类型替换成统一枚举

建议修改：

- [InternalUserEntity.java](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/model/entity/InternalUserEntity.java)

具体改法：

- `private InternalUserDataScopeType dataScopeType;`
  - 改为
  - `private UserDataScopeType dataScopeType;`
- `setDataScopeType(Integer dataScopeType)` 中：
  - `InternalUserDataScopeType.fromCode(...)`
  - 改为
  - `UserDataScopeType.fromCode(...)`
- `getDataScopeTypeEnum()` / `setDataScopeTypeEnum(...)`
  - 全部改为 `UserDataScopeType`

当前不改：

- [TeacherUserEntity.java](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/model/entity/TeacherUserEntity.java)
- [StudentUserEntity.java](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/model/entity/StudentUserEntity.java)

原因：

- 当前教师端、学员端不需要持久化 `dataScopeType`
- 其数据范围先由登录装载规则提供

完成标准：

- `InternalUserEntity` 完成替换
- Teacher / Student 实体保持不动

### 阶段 3：认证上下文替换

目标：

- 让认证链路中的用户快照和 `UserDetails` 统一使用 `UserDataScopeType`

建议修改：

- `backend/src/main/java/com/chordsked/backend/security/account/model/ChordSkedUserDetails.java`
- `backend/src/main/java/com/chordsked/backend/cache/security/SecurityCacheService.java`
- `backend/src/main/java/com/chordsked/backend/cache/security/impl/SecurityCacheServiceImpl.java`

具体改法：

1. `ChordSkedUserDetails`
   - 字段改为 `UserDataScopeType dataScopeType`
   - getter/setter 返回 `UserDataScopeType`

2. `SecurityUserSnapshot`
   - 快照字段改为 `UserDataScopeType dataScopeType`
   - 序列化/反序列化逻辑继续按 code 存储与读取

3. `SecurityCacheServiceImpl`
   - 读取快照时改用 `UserDataScopeType.fromCode(...)`
   - 写快照时改用 `userDataScopeType.getCode()`

注意：

- 如果已有缓存兼容逻辑，继续保留
- 不要在这一阶段改动 token 结构

完成标准：

- `UserDetails`、快照、缓存实现全部切换到 `UserDataScopeType`
- 相关缓存测试可通过

### 阶段 4：登录装载链路替换

目标：

- 让三端登录装载都能产出统一的 `UserDataScopeType`

建议修改：

- `backend/src/main/java/com/chordsked/backend/security/account/provider/InternalAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/TeacherAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/StudentAccountProvider.java`

具体规则：

1. `InternalAccountProvider`
   - 直接从 `InternalUserEntity.getDataScopeTypeEnum()` 读取统一后的 `UserDataScopeType`

2. `TeacherAccountProvider`
   - 当前阶段固定写入 `UserDataScopeType.CAMPUS`

3. `StudentAccountProvider`
   - 当前阶段固定写入 `UserDataScopeType.SELF`
   - 如果后续学员业务确认应按校区隔离，再调整为 `CAMPUS`

说明：

- 这里统一的是“进入框架层的表达”
- 不是要求三张用户表都长出相同字段

完成标准：

- 三端 `UserDetails` 均能提供 `UserDataScopeType`
- 相关 Provider 测试通过

### 阶段 5：DataScope 上下文与策略替换

目标：

- 让 DataScope 框架层彻底脱离 `InternalUserDataScopeType`

建议修改：

- `backend/src/main/java/com/chordsked/backend/datascope/context/DataScopeUserContext.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/DataScopeStrategy.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/AllDataScopeStrategy.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/CampusDataScopeStrategy.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/SelfDataScopeStrategy.java`

具体改法：

- `DataScopeUserContext` 中的数据范围字段统一改为 `UserDataScopeType`
- 所有策略类统一依赖 `UserDataScopeType`
- 删除对 `InternalUserDataScopeType` 的 import 和判断

完成标准：

- DataScope context / strategy 全部只依赖 `UserDataScopeType`

### 阶段 6：重构 `DataScopeInterceptor`

目标：

- 一次完成两件事：
  - 类型统一
  - 方法结构收敛

建议修改：

- [DataScopeInterceptor.java](file:///e:/Github/chordsked/backend/src/main/java/com/chordsked/backend/datascope/interceptor/DataScopeInterceptor.java)

建议结构收敛为 6 个方法：

1. `intercept(...)`
2. `getMappedStatement(...)`
3. `getDataScopeAnnotation(...)`
4. `buildUserContext(...)`
5. `buildScopeCondition(...)`
6. `appendCondition(...)`

具体改法：

- 合并 `findDataScope()` + `resolveDataScope()` -> `getDataScopeAnnotation()`
- 合并 `resolveCondition()` + `resolveStrategy()` -> `buildScopeCondition()`
- 合并 `findInsertIndex()` + `firstMatchStart()` + `containsWhere()` -> `appendCondition()`

日志建议：

- 注解解析失败：记录 `warn` 或 `error`
- 用户上下文缺失：记录 `warn`
- 不支持的数据范围类型：记录 `error`
- SQL 改写失败：记录 `error`

注意：

- 日志中不要打印明文 token
- 日志中不要打印敏感用户信息或完整安全快照
- 去掉只允许 `ADMIN` 的硬编码限制
- 改为基于 `ChordSkedUserDetails` 中统一的 `UserDataScopeType` 做策略分发

注意：

- 当前阶段仍可以保留：
  - 教师端默认 `CAMPUS`
  - 学员端默认 `SELF`
- 只要 `UserDetails` 里能取到统一类型即可

完成标准：

- `DataScopeInterceptor` 不再依赖 `InternalUserDataScopeType`
- 代码结构收敛完成
- 功能行为与当前实现一致

### 阶段 7：DTO / VO / 其他引用替换

目标：

- 清理所有仍残留的旧枚举引用

建议排查：

- 教务端账号查询返回 VO
- 安全相关 DTO
- 任何直接暴露数据范围字段的对象

建议操作：

1. 全局搜索 `InternalUserDataScopeType`
2. 所有非文档引用逐个替换为 `UserDataScopeType`
3. 删除旧枚举文件

完成标准：

- 代码中除文档外，不再存在 `InternalUserDataScopeType` 引用

### 阶段 8：测试回归

目标：

- 确保这次是“类型统一重构”，不是行为变更

必须覆盖：

1. 快照缓存测试
   - `SecurityCacheServiceImplTest`

2. 登录装载测试
   - `InternalAccountProviderTest`
   - `TeacherAccountProviderTest`
   - `StudentAccountProviderTest`

3. DataScope 策略测试
   - `DataScopeStrategyTest`

4. 鉴权与接口集成测试
   - `InternalUserControllerIntegrationTest`
   - `PermissionControllerIntegrationTest`
   - `RoleControllerIntegrationTest`

重点验证：

- `ALL` 仍可查询全部
- `CAMPUS` 仍按校区过滤
- `SELF` 仍仅返回本人
- teacher / student 登录装载后的 `UserDataScopeType` 正确

完成标准：

- 测试全部通过
- 无新增诊断错误

### 阶段 9：文档与清理

目标：

- 完成最后的收口，避免后续维护混乱

建议操作：

1. 删除：
   - `InternalUserDataScopeType.java`
2. 更新：
   - `datascope模块优化.md`
   - 其他 RBAC / DataScope 相关设计文档
3. 补充说明：
   - 教师端、学员端当前不持久化 `dataScopeType`
   - 但进入 DataScope 框架时仍统一转换为 `UserDataScopeType`

完成标准：

- 代码、测试、文档三者一致
- 后续开发者不会再困惑“到底该用哪个 dataScopeType”

## 12. 最终执行建议

如果按稳妥顺序推进，建议分两次提交：

### 第一批提交

- 新增 `UserDataScopeType`
- 替换 `InternalUserEntity`
- 替换 `ChordSkedUserDetails`
- 替换 `SecurityUserSnapshot`
- 替换三个 AccountProvider
- 修复相关测试

目标：

- 先把“统一类型体系”落稳

### 第二批提交

- 替换 `DataScopeUserContext`
- 替换策略类
- 重构 `DataScopeInterceptor`
- 清理旧枚举引用
- 回归 DataScope 与接口测试

目标：

- 再把“统一 DataScope 框架层”落稳

## 13. 一句话执行顺序

一句话版本：

1. 先建 `UserDataScopeType`
2. 再替换实体、`UserDetails`、缓存快照、登录装载
3. 再替换 DataScope 上下文、策略、拦截器
4. 最后删旧枚举、跑全量相关测试、更新文档
