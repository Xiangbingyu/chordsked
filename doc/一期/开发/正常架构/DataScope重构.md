# DataScope 最小重构清单

## 1. 目标

当前 `internal-users` 链路的数据权限是这样实现的：

- `Service` 从 `SecurityContextHolder` 读取当前用户
- `Service` 根据 `dataScopeType` 组装权限条件
- `Mapper XML` 手工写 `selfScope` / `campusScope` 相关 SQL

这能工作，但耦合点太多。最小重构目标不是一次性做成“完整基础框架”，而是只做一个最小闭环：

- 业务 `Service` 不再手工拼数据权限
- 业务 `Condition` 只保留业务查询条件
- 需要数据权限的 Mapper 通过统一注解声明
- 数据权限条件由 MyBatis 拦截器统一追加

一句话：把“接口里手写数据权限”收口到“框架层统一注入数据权限”。

## 2. 只保留最核心的 6 个类

建议第一版只保留下面这些类：

```text
backend/src/main/java/com/chordsked/backend/datascope/
├── annotation/
│   └── DataScope.java
├── context/
│   └── DataScopeUserContext.java
├── interceptor/
│   └── DataScopeInterceptor.java
└── strategy/
    ├── DataScopeStrategy.java
    ├── AllDataScopeStrategy.java
    ├── CampusDataScopeStrategy.java
    └── SelfDataScopeStrategy.java
```

说明：

- 第一版先不要上 `Factory`
- 第一版先不要上 `Metadata`
- 第一版先不要上 `SqlRewriter`
- 第一版先不要上 `SqlCache`
- 先把 `ALL / CAMPUS / SELF` 三种核心范围跑通

## 3. 这些类分别做什么

### 3.1 `DataScope.java`

作用：

- 标记某个 Mapper 方法需要数据权限处理

建议最小字段：

- `tableAlias`
- `scopeField`

示例：

```java
@DataScope(tableAlias = "u", scopeField = "id")
List<InternalUserQueryResultVO> listByQuery(@Param("condition") InternalUserQueryCondition condition);
```

### 3.2 `DataScopeUserContext.java`

作用：

- 表示当前登录人的数据权限上下文

建议保留字段：

- `userId`
- `userType`
- `dataScopeType`
- `campusIds`

说明：

- `campusIds` 只在 `SPECIFIED_CAMPUS` 时使用
- 第一版不需要预留太多扩展字段

### 3.3 `DataScopeStrategy.java`

作用：

- 定义统一策略接口，负责生成 SQL 条件

建议最小接口：

```java
public interface DataScopeStrategy {
    String buildCondition(DataScopeUserContext userContext, String tableAlias, String scopeField);
}
```

### 3.4 `AllDataScopeStrategy.java`

作用：

- 处理 `ALL_COMPANY`
- 返回空条件即可

### 3.5 `CampusDataScopeStrategy.java`

作用：

- 处理 `SPECIFIED_CAMPUS`
- 生成基于校区范围的过滤条件

说明：

- 第一版可以直接针对当前业务表结构实现
- 不必一开始追求抽象到支持所有表

### 3.6 `SelfDataScopeStrategy.java`

作用：

- 处理 `SELF_ONLY`
- 生成 `当前表字段 = 当前用户ID` 的过滤条件

### 3.7 `DataScopeInterceptor.java`

作用：

- 拦截被 `@DataScope` 标记的 Mapper 查询
- 读取当前用户上下文
- 根据 `dataScopeType` 选择策略
- 把策略生成的条件拼接到 SQL 中

说明：

- 第一版可以直接在拦截器里用 `if/else` 选择策略
- 不强求先做 `StrategyFactory`

## 4. 当前代码该怎么改

### 4.1 `InternalUserQueryServiceImpl`

重构后应删除：

- 当前用户数据范围判断
- `setSelfScope()`
- `setCampusScope()`
- `setScopeUserId()`
- `setScopeCampusIds()`

保留：

- 分页参数校验
- 状态参数校验
- 业务条件组装
- 结果聚合，如 `roleIds`

### 4.2 `InternalUserQueryCondition`

重构后只保留业务字段：

- `page`
- `pageSize`
- `keyword`
- `status`
- `roleId`
- `campusId`

应删除：

- `scopeUserId`
- `scopeCampusIds`
- `allScope`
- `campusScope`
- `selfScope`

### 4.3 `InternalUserMapper`

在需要隔离的方法上加注解：

```java
@DataScope(tableAlias = "u", scopeField = "id")
List<InternalUserQueryResultVO> listByQuery(@Param("condition") InternalUserQueryCondition condition);

@DataScope(tableAlias = "u", scopeField = "id")
Long countByQuery(@Param("condition") InternalUserQueryCondition condition);
```

### 4.4 `InternalUserMapper.xml`

重构后删除这些数据权限条件：

- `condition.campusScope`
- `condition.scopeCampusIds`
- `condition.selfScope`
- `condition.scopeUserId`

XML 最终只保留业务过滤：

- 关键词
- 状态
- 角色
- 校区

## 5. 最小重构步骤

建议按下面顺序做：

1. 先完成现有业务接口开发
2. 新增 `DataScope` 注解
3. 新增 `DataScopeUserContext`
4. 新增 `DataScopeStrategy` 和三个最小策略实现
5. 新增 `DataScopeInterceptor`
6. 让 `internal-users` 成为第一个试点模块
7. 删除当前 `Service + XML` 中手工拼接的数据权限逻辑
8. 跑通集成测试后，再迁移其他查询接口

## 6. 第一版先不要做的东西

下面这些都不是第一版必须项：

- `DataScopeStrategyFactory`
- `DataScopeMetadata`
- `DataScopeContextHolder`
- `DataScopeSqlRewriter`
- `DataScopeSqlCache`
- `DepartmentDataScopeStrategy`
- `AbacDataScopeStrategy`

这些可以放到第二阶段或第三阶段再补。

## 7. 验收标准

完成最小重构后，应满足：

- `Service` 不再显式判断 `ALL / CAMPUS / SELF`
- `Condition` 不再携带权限字段
- `Mapper XML` 不再手写 `selfScope/campusScope`
- `ALL_COMPANY` 仍然能查全部
- `SPECIFIED_CAMPUS` 仍然按绑定校区过滤
- `SELF_ONLY` 仍然只能查本人
- `listByQuery` 与 `countByQuery` 的过滤结果一致
- 现有 `internal-users` 接口行为不变

## 8. 当前建议

后续实际重构时，先做“最小版”就够了，不要一上来实现完整版目录。

更具体地说：

- 第一版先解决“统一注入数据权限”
- 第二版再考虑“工厂、元数据、SQL 缓存”
- 第三版再考虑“部门范围、ABAC、复杂 SQL 重写”

这样更适合当前项目节奏，也更容易稳定落地。
