package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.role.RoleUpdateRequest;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.service.role.RoleDetailQueryService;
import com.chordsked.backend.service.role.RoleUpdateService;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_update_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleUpdateServiceImplTest {
    @Autowired
    private RoleUpdateService roleUpdateService;

    @Autowired
    private RoleDetailQueryService roleDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateRoleAndPermissions() {
        RoleUpdateRequest request = buildValidRequest();

        roleUpdateService.update(request);

        RoleDetailQueryRequest detailRequest = new RoleDetailQueryRequest();
        detailRequest.setRoleId(1L);

        RoleDetailQueryResultVO result = roleDetailQueryService.getDetail(detailRequest);
        assertEquals("角色管理员", result.getName());
        assertEquals("更新后的角色描述", result.getDescription());
        assertEquals(4, result.getPermissionCount());
        assertEquals(List.of(1L, 120L, 121L, 125L), result.getPermissionIds());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status, request_params, response_result FROM sys_audit_log WHERE biz_id = ? ORDER BY id DESC LIMIT 1",
                1L
        );
        assertEquals("ROLE_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("UPDATE_ROLE", auditLog.get("action_type"));
        assertEquals(1L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_UPDATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"roleId\":1"));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":true"));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> roleUpdateService.update(null));
    }

    @Test
    void shouldThrowWhenRoleIdInvalid() {
        RoleUpdateRequest request = buildValidRequest();
        request.setRoleId(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("roleId必须大于0", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNameBlank() {
        RoleUpdateRequest request = buildValidRequest();
        request.setName("   ");

        assertThrows(BusinessException.class, () -> roleUpdateService.update(request));
    }

    @Test
    void shouldThrowWhenStatusInvalid() {
        RoleUpdateRequest request = buildValidRequest();
        request.setStatus(999);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("状态值无效", exception.getMessage());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT action_type, biz_id, status, error_msg, request_params, response_result FROM sys_audit_log WHERE action_type = 'UPDATE_ROLE' AND status = 0 ORDER BY id DESC LIMIT 1"
        );
        assertEquals("UPDATE_ROLE", auditLog.get("action_type"));
        assertEquals(1L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("状态值无效", auditLog.get("error_msg"));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_UPDATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"status\":999"));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":false"));
    }

    @Test
    void shouldThrowWhenRoleNotFound() {
        RoleUpdateRequest request = buildValidRequest();
        request.setRoleId(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("角色不存在", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPermissionIdsContainOnlyInvalidValues() {
        RoleUpdateRequest request = buildValidRequest();
        request.setPermissionIds(List.of(0L, -1L));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("permissionIds不能为空", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPermissionIdsContainUnknownPermission() {
        RoleUpdateRequest request = buildValidRequest();
        request.setPermissionIds(List.of(1L, 999L));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleUpdateService.update(request));

        assertEquals(400, exception.getCode());
        assertEquals("存在无效权限", exception.getMessage());
    }

    private RoleUpdateRequest buildValidRequest() {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRoleId(1L);
        request.setName("角色管理员");
        request.setDescription("更新后的角色描述");
        request.setStatus(1);
        request.setPermissionIds(List.of(1L, 120L, 121L, 125L));
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
