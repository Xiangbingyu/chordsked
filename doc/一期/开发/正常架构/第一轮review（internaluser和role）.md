# 第一轮 review（internaluser 和 role）

> 状态标识说明：`[已完成]` / `[进行中]` / `[待完成]`

## 1. review 目标

- 对后端 `internaluser` 与 `role` 两条 service 实现链路做一轮结构审查。
- 判断当前实现是否满足：结构统一、职责清晰、规则完整、长期可维护。
- 对照 `联调清单1.md`，确认还有哪些事项没有真正收口。
- 把前端“角色添加 / 删除按钮”纳入同一轮收口范围，形成后续执行步骤。

## 2. 当前结论

### 2.1 可以确认已经基本成型的部分

- `internaluser` 已经形成了相对完整的读写拆分：
  - 查询：`InternalUserQueryServiceImpl`
  - 详情：`InternalUserDetailQueryServiceImpl`
  - 创建：`InternalUserCreateServiceImpl`
  - 修改：`InternalUserUpdateServiceImpl`
  - 状态切换：`InternalUserStatusUpdateServiceImpl`
  - 重置密码：`InternalUserResetPasswordServiceImpl`
  - 目标账号校验：`InternalUserOperationGuardServiceImpl`
  - 缓存清理：`InternalUserCacheCleanupServiceImpl`
- `role` 也已经具备基础 CRUD 和详情查询能力：
  - 查询：`RoleQueryServiceImpl`
  - 详情：`RoleDetailQueryServiceImpl`
  - 创建：`RoleCreateServiceImpl`
  - 修改：`RoleUpdateServiceImpl`
  - 删除：`RoleDeleteServiceImpl`
- 两条链路都已经接入了审计注解、事务边界、DAO 分层调用，整体已经不是“缺能力”的阶段，而是“需要做结构收口和统一治理”的阶段。

### 2.2 当前更像是“能用”，但还没到“长期稳定维护”的状态

- `internaluser` 的主流程已经比较完整，创建 / 修改的角色与校区写入校验已完成第一轮收口，但基础字段校验和部分装配逻辑仍有重复。
- `role` 模块功能齐全，且写链路已完成一轮结构收口（`RoleOperationGuardService` / `RoleWriteValidator` / `RoleCacheCleanupService` 已落地）；并已完成“移除 `presetRole`、统一 `protectedRole`”改造。`RoleQueryServiceImpl` 的分页过滤与 `role` 模块异常风格统一也已完成。
- 前端角色页已完成“创建 / 修改 / 删除”三项核心操作链路，删除已接入二次确认、结果刷新和失败提示。
- `联调清单1.md` 中提到的事项里，“创建账号链路复用”已完成，本轮剩余重点主要是“真实环境联调验收”和“跨模块错误码及文案统一”。

## 3. 后端 review 发现

### 3.1 `internaluser` 链路的优点

- `InternalUserOperationGuardServiceImpl` 把“目标账号存在性、数据权限、保护账号、本人限制”收敛到了统一入口，这个方向是对的。
- `InternalUserCacheCleanupServiceImpl` 把资料更新、状态切换、密码重置后的缓存清理做了集中封装，避免写链路各自拼缓存逻辑。
- `update / status / resetPassword` 三条写链路都已经通过 guard service 复用统一校验，结构比直接在各 service 内散写判断更好。

### 3.2 `internaluser` 当前最需要处理的问题（[进行中]）

1. [进行中] 创建与修改链路的角色 / 校区写入校验已完成收口，但仍有局部重复待继续治理
   - `InternalUserWriteValidator` 已经落地，`InternalUserCreateServiceImpl` 与 `InternalUserUpdateServiceImpl` 现已统一复用：
     - `roleIds` 去重、存在性、启用状态校验
     - `protectedRoleCodes` 保护角色限制
     - `campusIds` 去重、存在性校验
     - `primaryCampusId` 必须属于 `campusIds`
   - 这一轮收口已经把最容易继续漂移的业务限制统一到了共享组件中。
   - 但以下内容仍然分散在 create / update 各自实现内：
     - 手机号 / 姓名 / 数据范围等基础字段校验
     - `UserRoleEntity` 批量构造
   - 后续如果继续新增字段约束或绑定装配规则，仍建议视复杂度继续下沉更细粒度的写入装配能力。

2. [已完成] 创建链路与修改链路对“系统保护角色”的限制已完成收口
   - `InternalUserCreateServiceImpl` 已补齐与 `InternalUserUpdateServiceImpl` 一致的限制：普通账号创建时同样不能分配受保护角色。
   - 这意味着“通过创建接口绕过前端限制、直接新建携带保护角色账号”的缺口已经被补上。
   - 这一条 P0 风险已经落地修复。
   - 当前该限制也已经与角色 / 校区写入校验一起下沉到 `InternalUserWriteValidator`，不再由 create / update 各自保留一份实现。

3. [待完成] 查询链路存在明显的装配重复与潜在 N+1 问题
   - `InternalUserQueryServiceImpl` 中的 `fillRoles` 会按角色逐个 `roleDao.getById()` 查角色名。
   - `InternalUserDetailQueryServiceImpl` 也在重复做同类角色名解析。
   - `fillOperationFlags` 又会对列表每个用户调用 `internalUserOperationGuardService.isProtectedUser()`，而这个方法内部会继续走 DAO 查询角色码。
   - 当前数据量小时问题不明显，但从长期维护看：
     - 性能不可预期
     - 查询装配逻辑分散
     - 列表 / 详情返回字段越多，重复代码会越来越多
   - 建议把“角色 ID / 角色名 / systemAccount / currentUser”装配统一下沉成专门 assembler 或批量查询能力。

4. [已完成] 状态切换里的规则重叠已经完成收口
   - `InternalUserStatusUpdateServiceImpl` 现已只保留 `validateOperationTarget` 这一条 guard 规则。
   - 原先禁用分支中“至少保留一个系统管理员”的不可达逻辑已经删除。
   - 当前语义已经明确为：只要是受保护账号，就直接拦截状态操作，不再额外叠加“最后一个系统管理员”规则。
   - 这一点减少了维护歧义，也避免后续误以为两套规则会同时生效。

5. [进行中] 参数校验异常类型不统一
   - 当前有的地方抛 `IllegalArgumentException`，有的地方抛 `BusinessException(ErrorCode.BAD_REQUEST, ...)`。
   - 对外效果会依赖全局异常映射，长期看不利于接口口径统一。
   - `internaluser` 模块虽然比 `role` 模块更接近统一中文文案，但异常类型仍建议收敛到统一规范。

### 3.3 `role` 链路的优点

- `RoleCreateServiceImpl` / `RoleUpdateServiceImpl` 都具备权限去重、有效性校验、事务写入，主流程完整。
- `RoleUpdateServiceImpl` 已经考虑到“角色权限变更后清理关联用户权限缓存”，这一点是维护性加分项。
- `RoleDeleteServiceImpl` 对“已绑定用户不可删”“受保护角色不可删”做了基础防护。

### 3.4 `role` 当前最需要处理的问题（[进行中]）

1. [已完成] 保护角色语义已完成统一，读写链路已采用 `protectedRoleCodes` 单一语义
   - 查询列表：`RoleQueryServiceImpl` 过滤 `protectedRoleCodes`（仍在 query service）
   - 查看详情：`RoleDetailQueryServiceImpl` 现已复用 `RoleOperationGuardService` 的 `isProtectedRoleCode(...)`
   - 创建 / 修改：`RoleCreateServiceImpl`、`RoleUpdateServiceImpl` 已通过 `RoleWriteValidator` + `RoleOperationGuardService` 统一限制 `protectedRoleCodes`
   - 删除：`RoleDeleteServiceImpl` 已通过 `RoleOperationGuardService` 统一限制 `protectedRoleCodes`
   - 配置侧已统一为 `protected-role-codes`，`preset-role-codes` 已移除；`system-admin-role-code` 也已移除，避免并行语义漂移。
   - `AuthorityCodeServiceImpl` 已改为基于 `protectedRoleCodes` 判断管理员全量权限，不再依赖单独的 `systemAdminRoleCode`。

2. [已完成] `RoleQueryServiceImpl` 的保护角色过滤已前移到 DAO / SQL 层
   - 当前已改为通过 SQL 条件直接排除 `protectedRoleCodes`，不再采用 service 层分页扫描 + 内存过滤。
   - 查询总数与列表数据已统一使用同一组过滤条件，分页语义更稳定、复杂度更低。
   - 由于列表查询已在 SQL 层直接排除 `protectedRoleCodes`，`RoleQueryResultVO` 不再额外暴露 `protectedRole` 字段。

3. [已完成] `role` 模块错误文案与异常风格已与 `internaluser` 对齐
   - `RoleQueryServiceImpl`、`RoleCreateServiceImpl`、`RoleUpdateServiceImpl`、`RoleDeleteServiceImpl`、`RoleDetailQueryServiceImpl`、`RoleWriteValidatorImpl`、`RoleOperationGuardServiceImpl` 的参数校验异常已统一为 `BusinessException(ErrorCode.BAD_REQUEST, ...)`。
   - 英文文案已替换为中文稳定文案（如“状态值无效”“角色编码不能为空”“角色名称长度不能超过50”）。
   - `RoleControllerIntegrationTest`、`RoleUpdateServiceImplTest`、`AuditLogServiceImplTest` 等断言已同步更新，避免回归。

4. [已完成] `RoleCreateServiceImpl`、`RoleUpdateServiceImpl` 的重复点已完成一轮收口
   - 权限 ID 去重校验、保护角色判断、字段长度与空值校验已统一复用 `RoleWriteValidator` / `RoleOperationGuardService`。
   - `RolePermissionEntity` 构造当前继续保留在 create / update 内部私有方法，不作为本轮强制抽取项。
   - 现阶段该重复量可接受，后续如角色写链路复杂度继续上升，再评估是否下沉为独立装配组件。

5. [已完成] `role` 链路缓存副作用已完成第一轮下沉，后续可扩展更多场景
   - `RoleCacheCleanupService` 已落地并接入 `RoleUpdateServiceImpl`，统一承接“角色变更后按角色关联用户进行权限缓存清理（事务提交后触发）”。
   - 当前结构已从“写链路内散写事务回调”提升为独立副作用服务，维护性明显改善。
   - 后续若引入角色删除/状态变更/批量授权等更多副作用场景，可继续沿 `RoleCacheCleanupService` 扩展，避免逻辑再次回流到具体 service。

6. [已完成] 局部页面文案已与当前能力同步
   - 前端角色页描述已移除“后续在此接入角色权限点编辑能力”表述。
   - 当前文案已明确页面能力：支持角色列表展示、创建角色、详情加载、修改提交；删除角色能力处于页面操作链路接入中。
   - review、联调与验收口径已与页面实际能力保持一致，减少认知偏差。

## 4. 对照 `联调清单1.md` 后仍未完成的问题

### 4.1 明确仍未收口的事项

1. [待完成] 真实环境联调验收
   - 文档结论更多还是基于代码完成度和测试覆盖。
   - 真实环境的配置加载、角色数据、校区数据、权限边界、缓存清理、副作用审计仍需要做一次完整走查。

2. [进行中] 跨模块错误码 / 错误文案统一
   - `internaluser` 模块已经统一了一轮。
   - `role` 模块已完成中文稳定文案 + `BusinessException(ErrorCode.BAD_REQUEST, ...)` 统一。
   - 当前主要剩余 `Student` 等其他模块的历史文案与异常口径治理。

### 4.2 当前已经不是问题的事项

- [已完成] `systemAccount`、`currentUser` 的前后端字段口径已经基本收口。
- [已完成] 账号详情和列表字段补齐已经完成。
- [已完成] 受保护账号和本人限制已经进入统一 guard 链路。
- [已完成] 创建链路已补齐“普通账号不得分配 `SYSTEM_ADMIN`”限制，不再存在创建接口绕过保护角色限制的问题。
- [已完成] `InternalUserCreateServiceImpl` 与 `InternalUserUpdateServiceImpl` 已通过 `InternalUserWriteValidator` 统一复用角色 / 校区写入校验。
- [已完成] 状态切换中的“至少保留一个系统管理员”分支已经删除，当前只保留 guard 对受保护账号的统一拦截。
- [已完成] 校区选项接口的权限复用也已经明确。

## 5. 前端角色“添加 / 删除按钮”review 结论

### 5.1 当前现状

- [已完成] “添加角色”已经有页面入口：
  - `RolePermissionOverview.tsx` 中存在“创建角色”按钮。
  - `RolePermissionPageContent.tsx` 已接入 `createRole()` 和创建弹窗。
- [已完成] “删除角色”后端和前端 service 已有基础能力：
  - 后端已有 `RoleDeleteServiceImpl`
  - 前端 `roleService.ts` 已有 `deleteRole(roleId)`
- [已完成] “删除角色”已补齐页面按钮，并接入二次确认、删除结果刷新、失败提示等完整交互。

### 5.2 这一块建议如何收口

1. 表格操作列补充“删除”
   - 在 `RolePermissionTableCard.tsx` 的操作列中增加“删除”按钮。

2. 页面层补充删除动作编排
   - 在 `RolePermissionPageContent.tsx` 中接入：
     - 删除确认
     - 删除请求
     - 成功后刷新列表
     - 失败提示

3. 文案与禁用态要和后端规则一致
   - 如果后端返回“系统保护角色不能删除”“角色已绑定用户，不能删除”，前端需要直接透传或稳定翻译。
   - 删除按钮如果后端未来补充“不可删标记”字段，前端要优先基于字段控制，而不是靠 message 猜。

4. 同步修正角色页说明文案
   - 页面描述不要再写“后续接入角色权限点编辑能力”。
   - 应改为反映当前真实能力：已支持创建、查看详情、修改；删除待补齐或已补齐。

## 6. 建议的收口步骤

### 步骤 1：先统一后端规则，不要先开按钮

- [已完成] 先收口 `InternalUserCreateServiceImpl` 与 `InternalUserUpdateServiceImpl` 的共享规则。
- [已完成] “创建链路未禁止分配 `SYSTEM_ADMIN`”这个不一致问题已修复，角色 / 校区写入校验也已通过 `InternalUserWriteValidator` 收口复用。
- [已完成] `InternalUserStatusUpdateServiceImpl` 已删掉“最后一个系统管理员”分支，当前剩余工作是保持保护账号规则语义稳定，不再重新分叉。

### 步骤 2：抽取两条链路中的公共策略

- `internaluser` 侧建议抽：
  - 基础字段校验 / 装配能力（如后续复杂度继续上升）
  - 查询结果装配器
- `role` 侧建议抽：
  - 保护角色策略
  - 角色目标校验服务
  - 权限绑定校验策略
  - 角色权限实体装配器
  - 角色变更缓存清理服务

### 步骤 3：统一异常类型与错误文案

- [进行中] 尽量减少 service 中直接抛 `IllegalArgumentException`。
- [进行中] 统一改成稳定的业务异常口径，至少做到：
  - 状态码稳定
  - message 稳定
  - 中文语义统一
- [已完成] `role` 模块已完成一轮收口；后续重点转向 `internaluser` / `student` 等模块。

### 步骤 4：把保护角色过滤前移到 DAO / SQL

- [已完成] `RoleQueryServiceImpl` 的分页扫描式过滤已下沉到 DAO / SQL。
- [待完成] `internaluser` 列表 / 详情里的角色装配和保护账号判断也尽量改成批量查询，减少 service 层拼装和循环 DAO 调用。

### 步骤 5：补齐前端删除角色按钮

- [已完成] 表格操作列已增加删除入口。
- [已完成] 页面层已接入确认弹窗和删除请求。
- [已完成] 删除成功后已刷新列表。
- [已完成] 删除失败时已展示后端稳定文案。

### 步骤 6：做一次真实环境联调验收

- [待完成] 校验 `application-common.yml` 是否在目标环境生效。
- [待完成] 校验测试环境里是否确实存在：
  - `SYSTEM_ADMIN`
  - 普通管理员
  - 普通教务账号
  - 校区绑定
  - 数据权限数据
- [待完成] 重点验证：
  - 保护账号不可编辑 / 禁用 / 重置密码
  - 本人账号不可编辑 / 禁用 / 重置密码
  - 创建链路字段约束与修改链路完全一致
  - 角色删除失败场景与后端规则一致

## 7. 本轮 review 的优先级建议

### P1

- 推进 `internaluser` / `student` 模块错误文案与异常类型统一。
- 完成真实环境联调验收与规则回归。

### P2

- 优化 `internaluser` 列表 / 详情的装配重复与潜在 N+1。
- 持续修正历史页面说明文案与实际能力偏差。

## 8. 本轮 review 后的验收口径

- `internaluser` 创建与修改必须复用同一套字段校验和业务限制。
- 当前角色 / 校区写入校验应统一经由 `InternalUserWriteValidator` 复用，不再允许 create / update 各自保留一份实现。
- 普通账号不得通过任何入口被赋予 `SYSTEM_ADMIN` 之类的保护角色。
- `internaluser` 状态切换只保留 guard 对受保护账号的统一拦截，不再并存“至少保留一个系统管理员”之类的重复规则。
- `internaluser` 与 `role` 两条写链路都应把“保护对象校验”和“缓存清理副作用”收敛成可复用的独立服务，而不是散落在各个 service 实现中。
- 上述收敛目标在当前代码中已完成第一轮落地：`internaluser` 与 `role` 写链路均已引入独立 guard / validator / cache cleanup 组件。
- `role` 模块的查询、详情、创建、修改、删除规则必须可通过一个统一策略解释清楚。
- `role` 模块“保护角色限制”应仅由 `protectedRoleCodes` 解释，不再保留 `presetRoleCodes` 等并行语义。
- `role` 模块错误文案不再中英混用，异常类型不再随实现人习惯漂移。
- 前端角色页至少要做到“创建、修改、删除”三项能力和后端链路一致。
- `联调清单1.md` 中与 `internaluser` 创建 / 修改规则复用相关的问题应视为已完成，剩余事项主要集中在真实环境验收和跨模块统一治理。
