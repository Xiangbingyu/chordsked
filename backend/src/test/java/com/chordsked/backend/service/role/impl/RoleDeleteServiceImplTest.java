package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.service.role.RoleCreateService;
import com.chordsked.backend.service.role.RoleDeleteService;
import com.chordsked.backend.service.role.RoleDetailQueryService;
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
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_delete_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleDeleteServiceImplTest {
    @Autowired
    private RoleDeleteService roleDeleteService;

    @Autowired
    private RoleCreateService roleCreateService;

    @Autowired
    private RoleDetailQueryService roleDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldThrowWhenDeletingPresetRole() {
        RoleDeleteRequest request = new RoleDeleteRequest();
        request.setRoleId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleDeleteService.delete(request));

        assertEquals(400, exception.getCode());
        assertEquals("系统预置角色不能删除", exception.getMessage());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT action_type, biz_id, status, error_msg, request_params, response_result FROM sys_audit_log WHERE action_type = 'DELETE_ROLE' AND status = 0 ORDER BY id DESC LIMIT 1"
        );
        assertEquals("DELETE_ROLE", auditLog.get("action_type"));
        assertEquals(1L, ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("系统预置角色不能删除", auditLog.get("error_msg"));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_DELETE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"roleId\":1"));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":false"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldThrowWhenRoleHasUserBinding() {
        Long roleId = createRole("DELETE_BOUND_ROLE");
        long now = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO sys_user_role (user_id, role_id, created_at, updated_at) VALUES (?, ?, ?, ?)",
                1001L, roleId, now, now
        );

        RoleDeleteRequest request = new RoleDeleteRequest();
        request.setRoleId(roleId);
        BusinessException exception = assertThrows(BusinessException.class, () -> roleDeleteService.delete(request));

        assertEquals(400, exception.getCode());
        assertEquals("角色已绑定用户，不能删除", exception.getMessage());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteRoleWhenValid() {
        Long roleId = createRole("DELETE_OK_ROLE");

        RoleDeleteRequest request = new RoleDeleteRequest();
        request.setRoleId(roleId);
        roleDeleteService.delete(request);

        RoleDetailQueryRequest detailRequest = new RoleDetailQueryRequest();
        detailRequest.setRoleId(roleId);
        BusinessException exception = assertThrows(BusinessException.class, () -> roleDetailQueryService.getDetail(detailRequest));

        assertEquals(400, exception.getCode());
        assertEquals("角色不存在", exception.getMessage());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status, request_params, response_result FROM sys_audit_log WHERE biz_id = ? AND action_type = 'DELETE_ROLE' ORDER BY id DESC LIMIT 1",
                roleId
        );
        assertEquals("ROLE_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("DELETE_ROLE", auditLog.get("action_type"));
        assertEquals(roleId.longValue(), ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_DELETE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"roleId\":" + roleId));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":true"));
    }

    private Long createRole(String code) {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode(code);
        request.setName("待删除角色");
        request.setDescription("删除测试角色");
        request.setStatus(1);
        request.setPermissionIds(List.of(1L, 120L));
        return roleCreateService.create(request);
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
