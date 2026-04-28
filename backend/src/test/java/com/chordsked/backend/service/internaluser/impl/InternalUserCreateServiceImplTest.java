package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.internaluser.InternalUserCreateRequest;
import com.chordsked.backend.service.internaluser.InternalUserCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_internal_user_create_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class InternalUserCreateServiceImplTest {
    @Autowired
    private InternalUserCreateService internalUserCreateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateInternalUserWithRolesAndCampuses() {
        InternalUserCreateRequest request = buildValidRequest();
        request.setUsername("campus_manager");
        request.setPhone("13800000021");
        request.setName("校区教务");

        Long userId = internalUserCreateService.create(request);

        assertNotNull(userId);

        Map<String, Object> userRow = jdbcTemplate.queryForMap(
                "SELECT username, phone, name, status, must_change_password, data_scope_type FROM sys_internal_user WHERE id = ?",
                userId
        );
        assertEquals("campus_manager", userRow.get("username"));
        assertEquals("13800000021", userRow.get("phone"));
        assertEquals("校区教务", userRow.get("name"));
        assertEquals(1, ((Number) userRow.get("status")).intValue());
        assertEquals(1, ((Number) userRow.get("must_change_password")).intValue());
        assertEquals(4, ((Number) userRow.get("data_scope_type")).intValue());

        Integer roleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_role WHERE user_id = ?",
                Integer.class,
                userId
        );
        Integer campusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_campus WHERE user_id = ?",
                Integer.class,
                userId
        );
        Integer primaryCampusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_campus WHERE user_id = ? AND campus_id = ? AND is_primary = 1",
                Integer.class,
                userId,
                1L
        );
        assertEquals(1, roleCount);
        assertEquals(1, campusCount);
        assertEquals(1, primaryCampusCount);

        Map<String, Object> auditLog = waitForAuditLog(
                """
                SELECT module_name, action_type, biz_id, status, request_params, response_result
                FROM sys_audit_log
                WHERE biz_id = ?
                ORDER BY id DESC
                LIMIT 1
                """,
                userId
        );
        assertEquals("INTERNAL_USER_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("CREATE_INTERNAL_USER", auditLog.get("action_type"));
        assertEquals(userId.longValue(), ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"INTERNAL_USER_CREATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"username\":\"campus_manager\""));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":true"));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"roleCount\":1"));
    }

    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        InternalUserCreateRequest request = buildValidRequest();
        request.setUsername("admin");

        BusinessException exception = assertThrows(BusinessException.class, () -> internalUserCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("用户名已存在", exception.getMessage());

        Map<String, Object> auditLog = waitForAuditLog(
                """
                SELECT action_type, status, error_msg, request_params, response_result
                FROM sys_audit_log
                WHERE action_type = 'CREATE_INTERNAL_USER'
                  AND status = 0
                  AND request_params LIKE '%"username":"admin"%'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        assertEquals("CREATE_INTERNAL_USER", auditLog.get("action_type"));
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("用户名已存在", auditLog.get("error_msg"));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"INTERNAL_USER_CREATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"username\":\"admin\""));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":false"));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> internalUserCreateService.create(null));
    }

    @Test
    void shouldThrowWhenPrimaryCampusMissingFromCampusIds() {
        InternalUserCreateRequest request = buildValidRequest();
        request.setCampusIds(List.of(2L));

        BusinessException exception = assertThrows(BusinessException.class, () -> internalUserCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("主校区必须在校区列表中", exception.getMessage());
    }

    @Test
    void shouldThrowWhenCampusNotExists() {
        InternalUserCreateRequest request = buildValidRequest();
        request.setCampusIds(List.of(999L));
        request.setPrimaryCampusId(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> internalUserCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("校区不存在: 999", exception.getMessage());
    }

    @Test
    void shouldThrowWhenAssigningProtectedRole() {
        InternalUserCreateRequest request = buildValidRequest();
        request.setRoleIds(List.of(3L));

        BusinessException exception = assertThrows(BusinessException.class, () -> internalUserCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("受保护角色不能分配给普通账号", exception.getMessage());
    }

    private InternalUserCreateRequest buildValidRequest() {
        InternalUserCreateRequest request = new InternalUserCreateRequest();
        request.setUsername("new_internal_user");
        request.setPhone("13800000020");
        request.setName("新教务");
        request.setAvatar("https://example.com/avatar.png");
        request.setRoleIds(List.of(1L));
        request.setCampusIds(List.of(1L));
        request.setPrimaryCampusId(1L);
        request.setDataScopeType(4);
        return request;
    }

    private Map<String, Object> waitForAuditLog(String sql, Object... args) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        RuntimeException latestException = null;
        while (System.nanoTime() < deadline) {
            try {
                return jdbcTemplate.queryForMap(sql, args);
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
}
