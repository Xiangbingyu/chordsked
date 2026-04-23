package com.chordsked.backend.datascope.interceptor;

import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.config.properties.DataScopeProperties;
import com.chordsked.backend.datascope.annotation.DataScope;
import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.datascope.strategy.DataScopeStrategy;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import jakarta.annotation.Resource;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component("dataScopeInterceptor")
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {java.sql.Connection.class, Integer.class})
})
public class DataScopeInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(DataScopeInterceptor.class);

    private final Map<String, Optional<DataScope>> annotationCache = new ConcurrentHashMap<>();
    private final Map<UserDataScopeType, DataScopeStrategy> strategyRoute;

    public DataScopeInterceptor(List<DataScopeStrategy> strategies) {
        Map<UserDataScopeType, DataScopeStrategy> route = new ConcurrentHashMap<>();
        for (DataScopeStrategy strategy : strategies) {
            DataScopeStrategy previousStrategy = route.put(strategy.getDataScopeType(), strategy);
            if (previousStrategy != null) {
                throw new IllegalStateException("Duplicate data scope strategy for data scope type: " + strategy.getDataScopeType());
            }
        }
        this.strategyRoute = route;
    }

    @Lazy
    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Resource(name = "dataScopeProperties")
    private DataScopeProperties dataScopeProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!dataScopeProperties.isEnabled()) {
            return invocation.proceed();
        }
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = metaObject.hasGetter("delegate.mappedStatement")
                ? (MappedStatement) metaObject.getValue("delegate.mappedStatement")
                : (MappedStatement) metaObject.getValue("mappedStatement");
        DataScope dataScope = resolveDataScope(mappedStatement.getId());
        if (dataScope == null) {
            return invocation.proceed();
        }

        String originalSql = statementHandler.getBoundSql().getSql();
        String sqlPlaceholder = dataScopeProperties.getSqlPlaceholder();
        if (!StringUtils.hasText(originalSql) || !originalSql.contains(sqlPlaceholder)) {
            String message = "data scope placeholder is missing: " + mappedStatement.getId();
            if (dataScopeProperties.isStrictPlaceholder()) {
                logger.error("Data scope placeholder is missing, statementId={}", mappedStatement.getId());
                throw new IllegalStateException(message);
            }
            logger.warn("Data scope placeholder is missing and strictPlaceholder=false, statementId={}", mappedStatement.getId());
            return invocation.proceed();
        }

        String condition = resolveCondition(buildUserContext(), dataScope);
        String replacement = "";
        if (StringUtils.hasText(condition)) {
            replacement = "AND (" + condition + ")";
        } else {
            logger.debug("Data scope skipped because strategy returned empty condition");
        }
        String scopedSql = originalSql.replace(sqlPlaceholder, replacement);
        metaObject.setValue("delegate.boundSql.sql", scopedSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

    private DataScope resolveDataScope(String statementId) {
        Optional<DataScope> optional = annotationCache.computeIfAbsent(statementId, id -> {
            int splitIndex = id.lastIndexOf('.');
            if (splitIndex <= 0 || splitIndex == id.length() - 1) {
                logger.debug("Data scope skipped because statementId is invalid, statementId={}", id);
                return Optional.empty();
            }
            String className = id.substring(0, splitIndex);
            String methodName = id.substring(splitIndex + 1);
            try {
                Class<?> mapperClass = Class.forName(className);
                List<Method> matchedMethods = Arrays.stream(mapperClass.getDeclaredMethods())
                        .filter(method -> method.getName().equals(methodName))
                        .toList();
                if (matchedMethods.size() > 1) {
                    boolean hasDataScopeAnnotation = matchedMethods.stream()
                            .anyMatch(method -> method.isAnnotationPresent(DataScope.class));
                    if (hasDataScopeAnnotation) {
                        logger.error("Resolve data scope mapper failed because overloaded mapper method is unsupported, statementId={}", id);
                        throw new IllegalStateException("overloaded mapper method is unsupported for @DataScope: " + id);
                    }
                }
                for (Method method : matchedMethods) {
                    if (method.isAnnotationPresent(DataScope.class)) {
                        return Optional.of(method.getAnnotation(DataScope.class));
                    }
                }
                logger.debug("Data scope skipped because annotation not found, statementId={}", id);
                return Optional.empty();
            } catch (ClassNotFoundException ex) {
                logger.error("Resolve data scope mapper failed, statementId={}", id, ex);
                throw new IllegalStateException("mapper class not found: " + className, ex);
            }
        });
        return optional.orElse(null);
    }

    private DataScopeUserContext buildUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Build data scope user context failed because authentication is missing");
            throw new IllegalStateException("authenticated user not found");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof ChordSkedUserDetails userDetails)) {
            logger.warn("Build data scope user context failed because principal is unsupported");
            throw new IllegalStateException("authenticated user not found");
        }
        UserDataScopeType dataScopeType = userDetails.getDataScopeTypeEnum();
        if (dataScopeType == null) {
            logger.warn(
                    "Build data scope user context failed because dataScopeType is missing, userType={}, userId={}",
                    userDetails.getUserTypeEnum(),
                    userDetails.getUserId()
            );
            throw new IllegalStateException("dataScopeType is missing");
        }
        List<Long> campusIds;
        if (!dataScopeType.isCampusScope()) {
            campusIds = List.of();
        } else if (AccountUserType.ADMIN.equals(userDetails.getUserTypeEnum())) {
            campusIds = userCampusDao.listCampusIdsByUserId(userDetails.getUserId());
        } else {
            Long currentCampusId = userDetails.getCurrentCampusId();
            campusIds = currentCampusId == null || currentCampusId <= 0 ? List.of() : List.of(currentCampusId);
        }
        return new DataScopeUserContext(
                userDetails.getUserId(),
                userDetails.getUserTypeEnum(),
                dataScopeType,
                campusIds
        );
    }

    private String resolveCondition(DataScopeUserContext userContext, DataScope dataScope) {
        UserDataScopeType dataScopeType = userContext.getDataScopeType();
        DataScopeStrategy strategy = strategyRoute.get(dataScopeType);
        if (strategy == null) {
            logger.warn("Resolve data scope strategy failed, dataScopeType={}", dataScopeType);
            throw new IllegalArgumentException("dataScopeType is unsupported");
        }
        return strategy.buildCondition(userContext, dataScope.tableAlias(), dataScope.scopeField());
    }
}
