package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_controller_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleControllerIntegrationTest {
    private static final Long USER_ID = 1001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @BeforeEach
    void setUp() {
        when(securityCacheService.isTokenRevoked(anyString())).thenReturn(false);
        when(securityCacheService.isTokenActive(anyString())).thenReturn(false);
        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot("ADMIN", USER_ID, true, 1L, null));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/admin/api/v1/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/admin/api/v1/roles")
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS_ADMIN\",\"name\":\"校区管理员\",\"description\":\"负责校区日常管理\",\"status\":1,\"permissionIds\":[1,120,121,125]}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/admin/api/v1/roles/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(delete("/admin/api/v1/roles/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnForbiddenWhenMissingRoleViewAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(delete("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnRoleListWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].code").value("ADMIN"))
                .andExpect(jsonPath("$.data.items[0].permissionCount").value(27))
                .andExpect(jsonPath("$.data.items[0].userCount").value(1))
                .andExpect(jsonPath("$.data.items[1].code").value("OPERATOR"))
                .andExpect(jsonPath("$.data.items[1].permissionCount").value(12))
                .andExpect(jsonPath("$.data.items[1].userCount").value(0));
    }

    @Test
    void shouldReturnBadRequestWhenRoleListPageInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleListPageSizeInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleListPageSizeTooLarge() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleListStatusInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", "999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnForbiddenWhenMissingRoleCreateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(post("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS_ADMIN\",\"name\":\"校区管理员\",\"description\":\"负责校区日常管理\",\"status\":1,\"permissionIds\":[1,120,121,125]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRolePermissionIdsEmpty() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:create"));

        mockMvc.perform(post("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS_ADMIN\",\"name\":\"校区管理员\",\"description\":\"负责校区日常管理\",\"status\":1,\"permissionIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateRoleWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:create", "admin:role:view"));

        mockMvc.perform(post("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS_ADMIN\",\"name\":\"校区管理员\",\"description\":\"负责校区日常管理\",\"status\":1,\"permissionIds\":[1,120,121,125]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    void shouldReturnRoleDetailWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("ADMIN"))
                .andExpect(jsonPath("$.data.name").value("系统管理员"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.permissionCount").value(27))
                .andExpect(jsonPath("$.data.userCount").value(1))
                .andExpect(jsonPath("$.data.permissionIds.length()").value(27))
                .andExpect(jsonPath("$.data.permissionIds[0]").value(1))
                .andExpect(jsonPath("$.data.permissionIds[26]").value(144))
                .andExpect(jsonPath("$.data.permissionTree.length()").value(6))
                .andExpect(jsonPath("$.data.permissionTree[0].code").value("admin:role"))
                .andExpect(jsonPath("$.data.permissionTree[3].children[4].code").value("admin:role:assign_permission"));
    }

    @Test
    void shouldReturnForbiddenWhenMissingRoleDeleteAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(delete("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenDeletingProtectedRole() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:delete"));

        mockMvc.perform(delete("/admin/api/v1/roles/3")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("系统保护角色不能删除"));
    }

    @Test
    void shouldReturnBadRequestWhenRoleDetailIdInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles/0")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleDetailNotFound() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/roles/999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    void shouldReturnForbiddenWhenMissingRoleUpdateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(put("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[1,120,121,125]}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenRoleUpdatePermissionIdsEmpty() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleUpdateNameTooLong() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"" + "a".repeat(51) + "\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[1,120]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleUpdateStatusInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":999,\"permissionIds\":[1,120]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值无效"));
    }

    @Test
    void shouldReturnBadRequestWhenRoleUpdateRoleIdInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/0")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[1,120]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenRoleUpdateRoleNotFound() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/999")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[1,120]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("角色不存在"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateRoleWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view", "admin:role:update"));

        mockMvc.perform(put("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"name\":\"角色管理员\",\"description\":\"更新后的角色描述\",\"status\":1,\"permissionIds\":[1,120,121,125]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/admin/api/v1/roles/1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("角色管理员"))
                .andExpect(jsonPath("$.data.description").value("更新后的角色描述"))
                .andExpect(jsonPath("$.data.permissionCount").value(4))
                .andExpect(jsonPath("$.data.permissionIds.length()").value(4))
                .andExpect(jsonPath("$.data.permissionIds[0]").value(1))
                .andExpect(jsonPath("$.data.permissionIds[1]").value(120))
                .andExpect(jsonPath("$.data.permissionIds[2]").value(121))
                .andExpect(jsonPath("$.data.permissionIds[3]").value(125));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteRoleWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:create", "admin:role:view", "admin:role:delete"));

        mockMvc.perform(post("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content("{\"code\":\"DELETE_ROLE\",\"name\":\"删除测试角色\",\"description\":\"待删除\",\"status\":1,\"permissionIds\":[1,120]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(delete("/admin/api/v1/roles/3")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/admin/api/v1/roles")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2));
    }
}
