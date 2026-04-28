# 第二轮 review（internaluser 和 role）

> 状态标识说明：`[已完成]` / `[进行中]` / `[待完成]`

## 1. 本轮结论（先说结论）

- 第一轮里 `role` 主线收口项已基本落地：保护角色语义统一、分页过滤下沉 SQL、异常风格统一、前端删除链路补齐，整体可视为“核心目标已完成”。
- 当前主要剩余工作集中在 `internaluser`：异常类型口径仍不统一、查询装配重复和潜在 N+1 仍存在，属于“结构可继续收口”的阶段。
- 前端 `org-rbac` 删除功能已可用，但页面文案与实际能力仍有一处轻微滞后；自动化测试覆盖也有明显空白。

## 2. 已完成项复核（对第一轮结论做回归确认）

1. [已完成] `role` 保护角色语义单一化
   - 已统一使用 `protectedRoleCodes`，并移除了并行语义配置 `system-admin-role-code`。
   - `AuthorityCodeServiceImpl` 已基于 `protectedRoleCodes` 判断管理员全量权限。

2. [已完成] `RoleQueryServiceImpl` 保护角色过滤下沉到 DAO / SQL
   - 不再使用 service 层分页扫描 + 内存过滤。
   - 总数查询与列表查询过滤条件一致，分页稳定性提升。

3. [已完成] `role` 模块异常类型与文案统一
   - `role` 相关 service/validator/guard 已收敛为 `BusinessException(ErrorCode.BAD_REQUEST, ...)`。
   - 文案已统一为中文稳定语义。

4. [已完成] 前端角色页删除链路
   - 操作列已接入“删除”按钮。
   - 已接入二次确认、删除请求、成功刷新、失败提示。

## 3. 第二轮发现（仍可继续收口）

### 3.1 后端：`internaluser` 异常口径未完全统一（[进行中]）

- 仍存在多处 `IllegalArgumentException`，与 `role` 的统一规范不一致。
- 典型位置（非穷举）：
  - `InternalUserCreateServiceImpl`
  - `InternalUserUpdateServiceImpl`
  - `InternalUserQueryServiceImpl`
  - `InternalUserDetailQueryServiceImpl`
  - `InternalUserStatusUpdateServiceImpl`
  - `InternalUserResetPasswordServiceImpl`
  - `InternalUserWriteValidatorImpl`（`primaryCampusId` 校验分支）
- 影响：
  - 对外错误口径依赖全局异常映射，行为不够可预期。
  - 文案与状态码长期易漂移，不利于跨模块统一治理。

### 3.2 后端：`internaluser` 查询装配重复与潜在 N+1（[待完成]）

- `InternalUserQueryServiceImpl` 仍在做 `fillRoles` + `fillOperationFlags` 的逐项装配。
- 角色名解析存在循环内按 ID 查询角色（`roleDao.getById(...)`）模式。
- `fillOperationFlags` 内逐个调用 `isProtectedUser(...)`，而 `isProtectedUser(...)` 会继续触发 DAO 查询角色码。
- 影响：
  - 数据量上来后性能不可预期。
  - 列表 / 详情在角色装配与保护标识上有重复逻辑，后续易继续分叉。

### 3.3 前端：角色页说明文案与实际能力有一处轻微滞后（[待完成]）

- `org-rbac` 页面描述当前写的是“删除角色能力正在页面操作链路中接入”。
- 但删除链路实际上已完成（按钮 + 确认 + 调接口 + 刷新 + 失败提示）。
- 建议把描述改成“已支持创建、修改（含详情数据回填）和删除角色”；详情入口与修改入口合并表达即可，无需单独按钮。

### 3.4 前端：`org-rbac` 页面缺少自动化测试（[待完成]）

- 当前未检索到 `RolePermissionPageContent` / `RolePermissionTableCard` 相关测试文件。
- 建议至少补 2 类最小价值测试：
  - 删除成功后触发列表刷新。
  - 删除失败时展示后端错误文案。

## 4. 收口优先级建议（第二轮）

### P1（建议本轮继续）

1. 统一 `internaluser` 异常口径
   - 把 `IllegalArgumentException` 收敛为 `BusinessException(ErrorCode.BAD_REQUEST, ...)`。
   - 同步统一中文稳定文案与测试断言。

2. 修正文案与联调口径
   - 修正 `org-rbac` 页头描述中的“删除正在接入”滞后表达。

### P2（建议下一轮）

1. 优化 `internaluser` 查询装配
   - 合并角色信息批量查询，减少循环 DAO 调用。
   - 把 `systemAccount/currentUser/roleNames` 装配统一下沉（assembler 或批量查询策略）。

2. 补前端关键回归测试
   - 覆盖删除成功/失败两条高风险交互路径。

## 5. 第二轮验收口径（建议）

- `role` 继续保持单一保护角色语义：仅由 `protectedRoleCodes` 解释。
- `internaluser` 与 `role` 在参数校验异常类型上保持同一规范。
- `internaluser` 列表/详情的角色与保护标识装配不再出现明显循环查询。
- 前端角色页文案与实际能力一致，避免 review/联调认知偏差。
- 删除角色交互在真实环境下验证通过（成功、失败、权限边界三类场景）。
