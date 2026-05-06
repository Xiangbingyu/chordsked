# 第三轮 Review：org / kafka / datascope

## 一、本轮结论

本轮 `datascope` 与 `org` 交叉链路已经完成到以下状态：

1. admin `ASSIGNED` 数据范围已经完全切换到 `OrgNode` 授权模型。
2. `DataScopeInterceptor` 对 `service/org` 的直接依赖已清理，框架层不再注入 `OrgDataScopeResolveService`。
3. `OrgDataScopeResolveService` 及其实现已删除，组织范围解析已迁入 `datascope` 包内部组件。
4. `authorizedCampusIds` / `currentCampusId` 已从 `datascope` 主链路、安全上下文与安全缓存快照中移除。
5. teacher/student 不再继续走 campus 型 datascope 语义；当前 `ASSIGNED` 范围先置空，后续再补 `OrgNode` 授权来源。

## 二、代码真实状态

### 2.1 当前 datascope 主链路

当前 `datascope` 主流程如下：

1. Mapper 方法打上 `@DataScope` 注解。
2. `DataScopeInterceptor` 在 MyBatis `StatementHandler.prepare` 阶段拦截 SQL。
3. 拦截器根据当前登录人构造 `DataScopeUserContext`。
4. 再根据 `UserDataScopeType` 路由到不同 `DataScopeStrategy` 生成 SQL 条件。
5. 最终把条件替换进 SQL 中的 `/*DATA_SCOPE*/` 占位符。

当前核心文件：

- `backend/src/main/java/com/chordsked/backend/datascope/interceptor/DataScopeInterceptor.java`
- `backend/src/main/java/com/chordsked/backend/datascope/context/DataScopeUserContext.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/AssignedDataScopeStrategy.java`
- `backend/src/main/java/com/chordsked/backend/datascope/resolver/OrgNodeDataScopeResolver.java`
- `backend/src/main/resources/mapper/internal/InternalUserMapper.xml`

### 2.2 框架层与 org 的分层问题已处理

当前状态如下：

1. `DataScopeInterceptor` 已经只依赖 `datascope` 包内组件，不再依赖 `service/org`
2. 原来的 `OrgDataScopeResolveService` 与 `OrgDataScopeResolveServiceImpl` 已删除
3. 组织范围解析逻辑已迁入：
   - `backend/src/main/java/com/chordsked/backend/datascope/resolver/OrgNodeDataScopeResolver.java`

当前 resolver 逻辑为：

1. 根据 `userId` 查询 `sys_user_org_scope`
2. 找出主归属组织 `primaryOrgNodeId`
3. 调用 `OrgNodeDao.listDescendantsByNodeId()` 展开授权节点及其后代
4. 返回：
   - `primaryOrgNodeId`
   - `authorizedOrgNodeIds`

说明：

- admin 的 `ASSIGNED` 数据范围已完全通过 `OrgNode` 解析
- `listDescendantsByNodeId()` 已确认包含当前节点自身，因此授权节点本身不会漏掉

### 2.3 authorizedCampusIds 已移除

`authorizedCampusIds` 已从以下位置移除：

- `DataScopeUserContext`
- `AssignedDataScopeStrategy`
- `DataScopeInterceptor`
- `datascope` 内部 resolver 返回结构

从当前代码看：

1. `@DataScope` 实际使用点仍只有 `InternalUserMapper`
2. 注解全部是 `@DataScope(tableAlias = "u", scopeField = "id")`
3. `AssignedDataScopeStrategy` 已不再包含 `campus_id`、`tableAlias = "c"` 或默认回退到 campus 的逻辑

结论：

- `datascope` 主链路已经不再维护 campus 型权限范围
- 当前数据权限语义已经收口到 `OrgNode`

### 2.4 currentCampusId 已从权限主链路移除

`currentCampusId` 已从以下结构中移除：

- `ChordSkedUserDetails`
- `SecurityCacheService.SecurityUserSnapshot`
- `SecurityCacheServiceImpl` 的新缓存写入格式
- `InternalAccountProvider`
- `DataScopeInterceptor`

当前说明：

- admin 的安全上下文与数据权限解析已经不再依赖 `currentCampusId`
- `SecurityCacheServiceImpl` 已兼容读取旧缓存格式，避免 Redis 中残留旧值导致读失败
- teacher/student 的 provider 也不再向 `datascope` 传 campus 语义

## 三、已完成项与剩余问题

### 3.1 已完成项

已完成：

- `datascope/interceptor -> service/org` 的依赖清理
- `OrgDataScopeResolveService` 删除
- `OrgNodeDataScopeResolver` 落入 `datascope` 内部
- `authorizedCampusIds` 清理
- `currentCampusId` 从 datascope 主链路、安全上下文、安全缓存快照中移除
- `AssignedDataScopeStrategy` 收敛为 `OrgNode-only`
- admin `ASSIGNED` 数据范围改为完全依赖 `sys_user_org_scope + OrgNode`

### 3.2 当前剩余问题

当前还未完成的只有 teacher/student 的 `OrgNode` 授权来源补齐，具体表现为：

- `TeacherUserEntity` 与 `StudentUserEntity` 当前仍只有 `campusId`
- teacher/student 当前没有 `orgNodeId` 字段，也没有类似 `sys_user_org_scope` 的组织授权来源
- 因此本轮按决策先把 teacher/student 的 `ASSIGNED` 范围置空

这意味着：

- admin 已经是最终目标模型
- teacher/student 目前只是暂时不具备 `OrgNode` 范围解析能力
- 后续需要为 teacher/student 补一个可以转换为 `authorizedOrgNodeIds` 的授权来源

## 四、改造判断

### 4.1 本轮建议保留的东西

建议保留：

- `@DataScope` 注解机制
- `DataScopeInterceptor`
- `DataScopeStrategy` 路由机制
- `UserDataScopeType` 三种策略模型
- `primaryOrgNodeId`
- `authorizedOrgNodeIds`

这些属于数据权限基础设施本体，仍然有价值。

### 4.2 本轮已移除或已收敛的东西

已完成移除或收敛：

- `service/org/OrgDataScopeResolveService`
- `service/org/impl/OrgDataScopeResolveServiceImpl`
- resolver 返回结构中的 `authorizedCampusIds`
- `DataScopeUserContext.authorizedCampusIds`
- `AssignedDataScopeStrategy` 中基于 campus 的分支
- `DataScopeInterceptor` 中基于 `currentCampusId` 构造范围的逻辑
- `ChordSkedUserDetails.currentCampusId`
- `SecurityUserSnapshot.currentCampusId`

说明：

- 上述清理已经落地完成
- teacher/student 虽然后续再补业务装配细节，但框架模型已经统一收口到 `OrgNode`

## 五、建议的目标结构

### 5.1 设计原则

目标原则：

1. framework 层只依赖 framework/datascope 自身抽象，不依赖 `service/org`
2. 数据范围上下文统一只表达 `OrgNode` 结果
3. admin、teacher、student 最终统一到 `OrgNode` 数据范围模型，campus 不再作为独立 datascope 语义保留

### 5.2 当前结构状态

当前已经实现的结构调整如下：

1. `OrgNode` 范围解析逻辑已收口到 `datascope` 包内
2. admin `datascope` 已完全走 `OrgNodeDataScopeResolver`
3. `AssignedDataScopeStrategy` 已变成 `OrgNode-only` 策略
4. teacher/student 当前不再携带 campus 型 datascope 语义，只等待后续补齐 `OrgNode` 授权来源

## 六、已完成步骤与后续步骤

### 6.1 已完成步骤

已完成：

- 收敛数据范围结果模型，只保留 org-node 维度
- 新增 `datascope` 内部 resolver，替代原 `service/org` 解析服务
- 删除 `OrgDataScopeResolveService` 与实现类
- 清理 `DataScopeInterceptor` 中的 service 依赖与 campus 分支
- 清理 `AssignedDataScopeStrategy` 的 campus 逻辑
- 清理 `ChordSkedUserDetails`、`SecurityUserSnapshot`、`InternalAccountProvider` 中的 `currentCampusId`
- 调整 `TeacherAccountProvider`、`StudentAccountProvider`，使其不再向 `datascope` 传递 campus 语义
- 删除 `InternalAccountProvider` 中的无效调用

### 6.2 后续步骤

后续只剩 teacher/student 的 `OrgNode` 授权来源补齐，建议拆成单独任务：

1. 明确 teacher/student 的组织主归属字段是否直接落到用户表
2. 或设计 teacher/student 专用的组织授权表
3. 最终统一转换为：
   - `primaryOrgNodeId`
   - `authorizedOrgNodeIds`
4. 接入现有 `datascope` 框架，不再新增新的 campus 型权限模型

## 七、影响面清单

### 7.1 已修改文件

- `backend/src/main/java/com/chordsked/backend/datascope/interceptor/DataScopeInterceptor.java`
- `backend/src/main/java/com/chordsked/backend/datascope/context/DataScopeUserContext.java`
- `backend/src/main/java/com/chordsked/backend/datascope/strategy/AssignedDataScopeStrategy.java`
- `backend/src/main/java/com/chordsked/backend/datascope/resolver/OrgNodeDataScopeResolver.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/InternalAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/TeacherAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/StudentAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/model/ChordSkedUserDetails.java`
- `backend/src/main/java/com/chordsked/backend/cache/security/SecurityCacheService.java`
- `backend/src/main/java/com/chordsked/backend/cache/security/impl/SecurityCacheServiceImpl.java`

### 7.2 已删除文件

- `backend/src/main/java/com/chordsked/backend/service/org/OrgDataScopeResolveService.java`
- `backend/src/main/java/com/chordsked/backend/service/org/impl/OrgDataScopeResolveServiceImpl.java`

### 7.3 需后续评估文件

- `backend/src/main/java/com/chordsked/backend/security/account/provider/TeacherAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/security/account/provider/StudentAccountProvider.java`
- `backend/src/main/java/com/chordsked/backend/model/entity/TeacherUserEntity.java`
- `backend/src/main/java/com/chordsked/backend/model/entity/StudentUserEntity.java`
- teacher/student 对应 mapper / schema

### 7.4 当前已确认的 @DataScope 使用点

当前只发现以下 Mapper 使用了 `@DataScope`：

- `backend/src/main/java/com/chordsked/backend/dao/mapper/InternalUserMapper.java`

说明本轮改造的实际 SQL 影响面相对可控，admin 主链路已经完成收敛。

## 八、风险与注意事项

### 8.1 Redis 快照格式变更风险

`SecurityUserSnapshot` 当前新格式已变为：

- enabled
- primaryOrgNodeId
- dataScopeType

同时代码中已兼容读取旧格式：

- 旧格式：`enabled|currentCampusId|primaryOrgNodeId|dataScopeType`
- 新格式：`enabled|primaryOrgNodeId|dataScopeType`

仍需注意：

- 读缓存解析逻辑
- 写缓存序列化逻辑
- 老缓存失效策略

建议方案：

- 可以直接清理旧缓存 key
- 即使不清，也不会导致当前代码读崩，因为已做兼容处理

### 8.2 teacher/student 迁移风险

当前 teacher/student 已不再依赖 `currentCampusId`，但也尚未拥有 `OrgNode` 授权来源。

当前真实状态是：

- teacher/student 的 `ASSIGNED` 范围当前先置空
- 若后续没有补齐 `OrgNode` 授权来源，它们将持续不具备组织范围数据权限

因此建议明确决策：

1. admin 本轮已完成 `OrgNode` 化重构
2. teacher/student 不再延续 campus 语义作为长期方案
3. teacher/student 后续补齐时，直接按 `OrgNode` 装配，不再回到 campus 模型

### 8.3 组织后代节点查询语义风险

当前组织范围展开依赖：

- `OrgNodeDao.listDescendantsByNodeId(nodeId)`

已确认该查询包含当前节点自身，因为 SQL 中明确包含：

- `id = #{nodeId}`

因此当前 admin `authorizedOrgNodeIds` 结果是完整的。

## 九、后续建议

建议按以下顺序继续推进：

1. 当前阶段结束，本轮 admin datascope 改造可收口
2. 后续单独评估 teacher/student 接入 `OrgNode` 模型所需的授权来源与装配方式
3. 为 teacher/student 补齐 `primaryOrgNodeId` / `authorizedOrgNodeIds`
4. 按同一模型接入现有 `datascope` 框架

## 十、最终建议

当前改造已经达到以下收口状态：

- `datascope` 只负责 `datascope`
- `org` 只提供组织数据存取能力，不再向框架层暴露权限解析 service
- 当前数据范围统一只认 `OrgNode`
- `authorizedCampusIds`、`currentCampusId` 已从主链路中删除
- admin 已落地，teacher/student 后续再补齐 `OrgNode` 授权来源

如果按这个方向继续推进，`datascope` 已经从“混合 org/campus 两种时代痕迹的过渡实现”收敛为“admin 已完成、teacher/student 后续接入的 `OrgNode` 统一数据权限框架”。
