package com.chordsked.backend.service.audit.impl;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import com.chordsked.backend.model.enums.AuditLogStatus;
import com.chordsked.backend.service.audit.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_audit_log_service_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class AuditLogServiceImplTest {
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TransactionTemplate transactionTemplate;

    @Autowired
    void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM sys_audit_log");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldRecordSuccessAuditAfterTransactionCommit() {
        transactionTemplate.executeWithoutResult(status -> auditLogService.record(buildAuditLogRecordRequest(
                "ROLE_MANAGEMENT",
                "CREATE_ROLE",
                101L,
                "{\"roleId\":101}",
                "{\"created\":true}",
                AuditLogStatus.SUCCESS.getCode(),
                null
        )));

        Map<String, Object> auditLog = loadLatestAuditLog();
        assertEquals("ROLE_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("CREATE_ROLE", auditLog.get("action_type"));
        assertEquals(101L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertEquals(1001L, ((Number) auditLog.get("user_id")).longValue());
        assertEquals("ADMIN", auditLog.get("user_type"));
        assertEquals("POST", auditLog.get("request_method"));
        assertEquals("/admin/api/v1/roles", auditLog.get("request_uri"));
    }

    @Test
    void shouldRecordFailureAuditAfterTransactionRollback() {
        transactionTemplate.executeWithoutResult(status -> {
            auditLogService.record(buildAuditLogRecordRequest(
                    "ROLE_MANAGEMENT",
                    "UPDATE_ROLE",
                    1L,
                    "{\"roleId\":1,\"status\":999}",
                    null,
                    AuditLogStatus.FAILED.getCode(),
                    "状态值无效",
                    "/admin/api/v1/roles/1",
                    "PUT",
                    1001L,
                    "ADMIN:1001",
                    "ADMIN"
            ));
            status.setRollbackOnly();
        });

        Map<String, Object> auditLog = loadLatestAuditLog();
        assertEquals("UPDATE_ROLE", auditLog.get("action_type"));
        assertEquals(1L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("状态值无效", auditLog.get("error_msg"));
        assertEquals("/admin/api/v1/roles/1", auditLog.get("request_uri"));
    }

    @Test
    void shouldRecordSuccessAuditWithoutTransaction() {
        AuditLogRecordRequest request = new AuditLogRecordRequest();
        request.setModuleName("ROLE_MANAGEMENT");
        request.setActionType("DELETE_ROLE");
        request.setBizId(9L);
        request.setRequestParams("{\"roleId\":9}");
        request.setResponseResult("{\"deleted\":true}");
        request.setStatus(AuditLogStatus.SUCCESS.getCode());

        auditLogService.record(request);

        Map<String, Object> auditLog = loadLatestAuditLog();
        assertEquals("DELETE_ROLE", auditLog.get("action_type"));
        assertEquals(9L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertNull(auditLog.get("request_method"));
    }

    @Test
    void shouldRecordAuditWithoutSecurityAndRequestContext() {
        AuditLogRecordRequest request = new AuditLogRecordRequest();
        request.setModuleName("ROLE_MANAGEMENT");
        request.setActionType("CREATE_ROLE");
        request.setBizId(202L);
        request.setRequestParams("{\"roleId\":202}");
        request.setResponseResult("{\"created\":true}");
        request.setStatus(AuditLogStatus.SUCCESS.getCode());

        auditLogService.record(request);

        Map<String, Object> auditLog = loadLatestAuditLog();
        assertEquals("CREATE_ROLE", auditLog.get("action_type"));
        assertEquals(202L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertNull(auditLog.get("user_id"));
        assertNull(auditLog.get("request_uri"));
    }

    private Map<String, Object> loadLatestAuditLog() {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        RuntimeException latestException = null;
        while (System.nanoTime() < deadline) {
            try {
                return jdbcTemplate.queryForMap(
                        "SELECT module_name, action_type, biz_id, status, error_msg, user_id, user_type, request_method, request_uri " +
                                "FROM sys_audit_log ORDER BY id DESC LIMIT 1"
                );
            } catch (RuntimeException exception) {
                latestException = exception;
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting audit log persistence", interruptedException);
                }
            }
        }
        if (latestException != null) {
            throw latestException;
        }
        throw new IllegalStateException("Audit log not found in expected time window");
    }

    private AuditLogRecordRequest buildAuditLogRecordRequest(
            String moduleName,
            String actionType,
            Long bizId,
            String requestParams,
            String responseResult,
            Integer status,
            String errorMsg
    ) {
        return buildAuditLogRecordRequest(
                moduleName,
                actionType,
                bizId,
                requestParams,
                responseResult,
                status,
                errorMsg,
                "/admin/api/v1/roles",
                "POST",
                1001L,
                "ADMIN:1001",
                "ADMIN"
        );
    }

    private AuditLogRecordRequest buildAuditLogRecordRequest(
            String moduleName,
            String actionType,
            Long bizId,
            String requestParams,
            String responseResult,
            Integer status,
            String errorMsg,
            String requestUri,
            String requestMethod,
            Long userId,
            String userName,
            String userType
    ) {
        AuditLogRecordRequest request = new AuditLogRecordRequest();
        request.setModuleName(moduleName);
        request.setActionType(actionType);
        request.setBizId(bizId);
        request.setUserId(userId);
        request.setUserName(userName);
        request.setUserType(userType);
        request.setRequestUri(requestUri);
        request.setRequestMethod(requestMethod);
        request.setRequestIp("127.0.0.1");
        request.setUserAgent("JUnit");
        request.setRequestParams(requestParams);
        request.setResponseResult(responseResult);
        request.setStatus(status);
        request.setErrorMsg(errorMsg);
        return request;
    }
}
