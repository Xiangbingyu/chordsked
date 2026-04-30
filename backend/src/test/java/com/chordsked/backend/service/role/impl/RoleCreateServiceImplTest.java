package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.service.role.RoleCreateService;
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
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_create_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleCreateServiceImplTest {
    @Autowired
    private RoleCreateService roleCreateService;

    @Autowired
    private RoleDetailQueryService roleDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateRoleWithPermissions() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("CAMPUS_ADMIN");
        request.setName("组织管理员");
        request.setDescription("负责校区日常管理");
        request.setStatus(1);
        request.setPermissionIds(List.of(1L, 120L, 121L, 125L));

        Long roleId = roleCreateService.create(request);

        RoleDetailQueryRequest detailRequest = new RoleDetailQueryRequest();
        detailRequest.setRoleId(roleId);
        RoleDetailQueryResultVO result = roleDetailQueryService.getDetail(detailRequest);
        assertEquals("CAMPUS_ADMIN", result.getCode());
        assertEquals("组织管理员", result.getName());
        assertEquals(4, result.getPermissionCount());
        assertEquals(List.of(1L, 120L, 121L, 125L), result.getPermissionIds());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status, request_params, response_result FROM sys_audit_log WHERE biz_id = ? ORDER BY id DESC LIMIT 1",
                roleId
        );
        assertEquals("ROLE_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("CREATE_ROLE", auditLog.get("action_type"));
        assertEquals(roleId.longValue(), ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_CREATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"code\":\"CAMPUS_ADMIN\""));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":true"));
    }

    @Test
    void shouldThrowWhenRoleCodeAlreadyExists() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("ADMIN");
        request.setName("新管理员");
        request.setDescription("重复编码");
        request.setStatus(1);
        request.setPermissionIds(List.of(1L, 120L));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("角色编码已存在", exception.getMessage());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT action_type, status, error_msg, request_params, response_result FROM sys_audit_log WHERE action_type = 'CREATE_ROLE' AND status = 0 ORDER BY id DESC LIMIT 1"
        );
        assertEquals("CREATE_ROLE", auditLog.get("action_type"));
        assertEquals(0, ((Number) auditLog.get("status")).intValue());
        assertEquals("角色编码已存在", auditLog.get("error_msg"));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"ROLE_CREATE\""));
        assertEquals(true, ((String) auditLog.get("request_params")).contains("\"code\":\"ADMIN\""));
        assertEquals(true, ((String) auditLog.get("response_result")).contains("\"success\":false"));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> roleCreateService.create(null));
    }

    @Test
    void shouldThrowWhenCodeBlank() {
        RoleCreateRequest request = buildValidRequest();
        request.setCode("   ");

        assertThrows(BusinessException.class, () -> roleCreateService.create(request));
    }

    @Test
    void shouldThrowWhenPermissionIdsEmpty() {
        RoleCreateRequest request = buildValidRequest();
        request.setPermissionIds(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> roleCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("permissionIds不能为空", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPermissionIdsContainUnknownPermission() {
        RoleCreateRequest request = buildValidRequest();
        request.setPermissionIds(List.of(1L, 999L));

        BusinessException exception = assertThrows(BusinessException.class, () -> roleCreateService.create(request));

        assertEquals(400, exception.getCode());
        assertEquals("存在无效权限", exception.getMessage());
    }

    private RoleCreateRequest buildValidRequest() {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode("CAMPUS_ADMIN");
        request.setName("组织管理员");
        request.setDescription("负责校区日常管理");
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
