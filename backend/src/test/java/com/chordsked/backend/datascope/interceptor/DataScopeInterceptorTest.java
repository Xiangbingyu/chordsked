package com.chordsked.backend.datascope.interceptor;

import com.chordsked.backend.config.properties.DataScopeProperties;
import com.chordsked.backend.datascope.annotation.DataScope;
import com.chordsked.backend.datascope.resolver.OrgNodeDataScopeResolver;
import com.chordsked.backend.datascope.strategy.AssignedDataScopeStrategy;
import com.chordsked.backend.datascope.strategy.AllDataScopeStrategy;
import com.chordsked.backend.datascope.strategy.SelfDataScopeStrategy;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataScopeInterceptorTest {
    private DataScopeInterceptor interceptor;
    private OrgNodeDataScopeResolver orgNodeDataScopeResolver;
    private DataScopeProperties dataScopeProperties;

    @BeforeEach
    void setUp() {
        interceptor = new DataScopeInterceptor(List.of(
                new AllDataScopeStrategy(),
                new AssignedDataScopeStrategy(),
                new SelfDataScopeStrategy()
        ));
        orgNodeDataScopeResolver = mock(OrgNodeDataScopeResolver.class);
        dataScopeProperties = new DataScopeProperties();
        dataScopeProperties.setEnabled(true);
        dataScopeProperties.setStrictPlaceholder(true);
        dataScopeProperties.setSqlPlaceholder("/*DATA_SCOPE*/");

        ReflectionTestUtils.setField(interceptor, "orgNodeDataScopeResolver", orgNodeDataScopeResolver);
        ReflectionTestUtils.setField(interceptor, "dataScopeProperties", dataScopeProperties);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipWhenMapperMethodHasNoDataScopeAnnotation() throws Throwable {
        FakeStatementHandler statementHandler = createStatementHandler(
                PlainMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u"
        );

        Object result = interceptor.intercept(createInvocation(statementHandler));

        assertNull(result);
        assertEquals("SELECT * FROM sys_user u", statementHandler.getBoundSql().getSql());
    }

    @Test
    void shouldAppendSelfConditionWhenSqlHasNoWhere() throws Throwable {
        FakeStatementHandler statementHandler = createStatementHandler(
                SelfScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE 1 = 1 /*DATA_SCOPE*/"
        );
        authenticate(new ChordSkedUserDetails(
                1001L,
                AccountUserType.ADMIN,
                null,
                UserDataScopeType.SELF,
                true,
                List.of()
        ));

        interceptor.intercept(createInvocation(statementHandler));

        assertEquals(
                "SELECT * FROM sys_user u WHERE 1 = 1 AND (u.id = 1001)",
                statementHandler.getBoundSql().getSql()
        );
    }

    @Test
    void shouldReplacePlaceholderWithAssignedOrgNodeCondition() throws Throwable {
        FakeStatementHandler statementHandler = createStatementHandler(
                AssignedScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE u.status = 1 /*DATA_SCOPE*/ ORDER BY u.id DESC LIMIT 10"
        );
        when(orgNodeDataScopeResolver.resolveByUserId(1001L))
                .thenReturn(new OrgNodeDataScopeResolver.OrgNodeDataScopeResult(11L, List.of(11L, 22L)));
        authenticate(new ChordSkedUserDetails(
                1001L,
                AccountUserType.ADMIN,
                11L,
                UserDataScopeType.ASSIGNED,
                true,
                List.of()
        ));

        interceptor.intercept(createInvocation(statementHandler));

        assertEquals(
                "SELECT * FROM sys_user u WHERE u.status = 1 AND (EXISTS (SELECT 1 FROM sys_user_org_scope ds_uos WHERE ds_uos.user_id = u.id AND ds_uos.org_node_id IN (11, 22))) ORDER BY u.id DESC LIMIT 10",
                statementHandler.getBoundSql().getSql()
        );
    }

    @Test
    void shouldDenyAllWhenNonAdminAssignedHasNoOrgNodeRange() throws Throwable {
        FakeStatementHandler statementHandler = createStatementHandler(
                AssignedScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE u.status = 1 /*DATA_SCOPE*/"
        );
        authenticate(new ChordSkedUserDetails(
                2001L,
                AccountUserType.TEACHER,
                null,
                UserDataScopeType.ASSIGNED,
                true,
                List.of()
        ));

        interceptor.intercept(createInvocation(statementHandler));

        assertEquals(
                "SELECT * FROM sys_user u WHERE u.status = 1 AND (1 = 0)",
                statementHandler.getBoundSql().getSql()
        );
    }

    @Test
    void shouldReplacePlaceholderForOuterQueryWithSubQuery() throws Throwable {
        FakeStatementHandler statementHandler = createStatementHandler(
                SelfScopedMapper.class.getName() + ".list",
                "SELECT * FROM (SELECT * FROM sys_user su WHERE su.status = 1) u WHERE 1 = 1 /*DATA_SCOPE*/"
        );
        authenticate(new ChordSkedUserDetails(
                1001L,
                AccountUserType.ADMIN,
                null,
                UserDataScopeType.SELF,
                true,
                List.of()
        ));

        interceptor.intercept(createInvocation(statementHandler));

        assertEquals(
                "SELECT * FROM (SELECT * FROM sys_user su WHERE su.status = 1) u WHERE 1 = 1 AND (u.id = 1001)",
                statementHandler.getBoundSql().getSql()
        );
    }

    @Test
    void shouldThrowWhenDataScopePlaceholderMissing() {
        FakeStatementHandler statementHandler = createStatementHandler(
                SelfScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE 1 = 1"
        );
        authenticate(new ChordSkedUserDetails(
                1001L,
                AccountUserType.ADMIN,
                null,
                UserDataScopeType.SELF,
                true,
                List.of()
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> interceptor.intercept(createInvocation(statementHandler))
        );

        assertEquals(
                "data scope placeholder is missing: " + SelfScopedMapper.class.getName() + ".list",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenAuthenticationMissing() {
        FakeStatementHandler statementHandler = createStatementHandler(
                SelfScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE 1 = 1 /*DATA_SCOPE*/"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> interceptor.intercept(createInvocation(statementHandler))
        );

        assertEquals("authenticated user not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenDataScopeTypeMissing() {
        FakeStatementHandler statementHandler = createStatementHandler(
                SelfScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE 1 = 1 /*DATA_SCOPE*/"
        );
        authenticate(new ChordSkedUserDetails(
                1001L,
                AccountUserType.ADMIN,
                null,
                null,
                true,
                List.of()
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> interceptor.intercept(createInvocation(statementHandler))
        );

        assertEquals("dataScopeType is missing", exception.getMessage());
    }

    @Test
    void shouldThrowWhenMapperMethodIsOverloadedWithDataScopeAnnotation() {
        FakeStatementHandler statementHandler = createStatementHandler(
                OverloadedScopedMapper.class.getName() + ".list",
                "SELECT * FROM sys_user u WHERE 1 = 1 /*DATA_SCOPE*/"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> interceptor.intercept(createInvocation(statementHandler))
        );

        assertEquals(
                "overloaded mapper method is unsupported for @DataScope: "
                        + OverloadedScopedMapper.class.getName() + ".list",
                exception.getMessage()
        );
    }

    private void authenticate(ChordSkedUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Invocation createInvocation(StatementHandler statementHandler) {
        try {
            return new Invocation(
                    statementHandler,
                    StatementHandler.class.getMethod("prepare", Connection.class, Integer.class),
                    new Object[]{null, null}
            );
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private FakeStatementHandler createStatementHandler(String statementId, String sql) {
        Configuration configuration = new Configuration();
        StaticSqlSource sqlSource = new StaticSqlSource(configuration, sql);
        MappedStatement mappedStatement = new MappedStatement.Builder(
                configuration,
                statementId,
                sqlSource,
                SqlCommandType.SELECT
        ).build();
        BoundSql boundSql = new BoundSql(configuration, sql, List.of(), new Object());
        return new FakeStatementHandler(mappedStatement, boundSql);
    }

    private interface PlainMapper {
        void list();
    }

    private interface SelfScopedMapper {
        @DataScope(tableAlias = "u", scopeField = "id")
        void list();
    }

    private interface AssignedScopedMapper {
        @DataScope(tableAlias = "u", scopeField = "id")
        void list();
    }

    private interface OverloadedScopedMapper {
        @DataScope(tableAlias = "u", scopeField = "id")
        void list();

        @DataScope(tableAlias = "u", scopeField = "id")
        void list(Long userId);
    }

    private static final class FakeStatementHandler implements StatementHandler {
        private final Delegate delegate;

        private FakeStatementHandler(MappedStatement mappedStatement, BoundSql boundSql) {
            this.delegate = new Delegate(mappedStatement, boundSql);
        }

        public Delegate getDelegate() {
            return delegate;
        }

        @Override
        public Statement prepare(Connection connection, Integer transactionTimeout) {
            return null;
        }

        @Override
        public void parameterize(Statement statement) {
        }

        @Override
        public void batch(Statement statement) {
        }

        @Override
        public int update(Statement statement) {
            return 0;
        }

        @Override
        public <E> List<E> query(Statement statement, ResultHandler resultHandler) {
            return List.of();
        }

        @Override
        public <E> Cursor<E> queryCursor(Statement statement) {
            return null;
        }

        @Override
        public BoundSql getBoundSql() {
            return delegate.getBoundSql();
        }

        @Override
        public ParameterHandler getParameterHandler() {
            return null;
        }
    }

    private static final class Delegate {
        private final MappedStatement mappedStatement;
        private final BoundSql boundSql;

        private Delegate(MappedStatement mappedStatement, BoundSql boundSql) {
            this.mappedStatement = mappedStatement;
            this.boundSql = boundSql;
        }

        public MappedStatement getMappedStatement() {
            return mappedStatement;
        }

        public BoundSql getBoundSql() {
            return boundSql;
        }
    }
}
