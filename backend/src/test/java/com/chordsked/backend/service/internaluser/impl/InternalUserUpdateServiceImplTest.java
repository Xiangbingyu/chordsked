package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.internaluser.InternalUserUpdateRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import com.chordsked.backend.service.internaluser.InternalUserUpdateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_internal_user_update_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class InternalUserUpdateServiceImplTest {
    @Autowired
    private InternalUserUpdateService internalUserUpdateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        authenticateAsAdmin(1001L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectAssigningProtectedRoleWhenUpdatingInternalUser() {
        InternalUserUpdateRequest request = buildValidRequest();
        request.setRoleIds(List.of(3L));

        BusinessException exception = assertThrows(BusinessException.class, () -> internalUserUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("受保护角色不能分配给普通账号", exception.getMessage());

        Integer systemAdminRoleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
                Integer.class,
                1002L,
                3L
        );
        assertEquals(0, systemAdminRoleCount);

        Map<String, Object> auditLog = waitForAuditLog(
                """
                SELECT action_type, status, error_msg, request_params, response_result
                FROM sys_audit_log
                WHERE action_type = 'UPDATE_INTERNAL_USER'
                  AND status = 0
                  AND request_params LIKE '%"userId":1002%'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        assertEquals("UPDATE_INTERNAL_USER", auditLog.get("action_type"));
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("受保护角色不能分配给普通账号", auditLog.get("error_msg"));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":false"));
    }

    private InternalUserUpdateRequest buildValidRequest() {
        InternalUserUpdateRequest request = new InternalUserUpdateRequest();
        request.setUserId(1002L);
        request.setPhone("13800000001");
        request.setName("杭州教务一");
        request.setAvatar("https://example.com/avatar.png");
        request.setRoleIds(List.of(2L));
        request.setPrimaryOrgNodeId(1L);
        request.setOrgScopeNodeIds(List.of(1L));
        request.setDataScopeType(2);
        return request;
    }

    private void authenticateAsAdmin(Long userId) {
        ChordSkedUserDetails userDetails = new ChordSkedUserDetails(
                userId,
                AccountUserType.ADMIN,
                1L,
                1L,
                UserDataScopeType.ALL,
                true,
                List.of()
        );
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
