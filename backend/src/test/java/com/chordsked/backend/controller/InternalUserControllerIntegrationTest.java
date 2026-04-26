package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.utils.cookie.CookieUtils;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_internal_user_controller_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class InternalUserControllerIntegrationTest {
    private static final Long USER_ID = 1001L;
    private static final long NOW = 1774483200000L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @BeforeEach
    void setUp() {
        when(securityCacheService.isTokenRevoked(anyString())).thenReturn(false);
        when(securityCacheService.isTokenActive(anyString())).thenReturn(false);
        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        USER_ID,
                        true,
                        1L,
                        UserDataScopeType.ALL_COMPANY
                ));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/admin/api/v1/internal-users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnForbiddenWhenMissingFineGrainedAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenListStatusInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .param("status", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值无效"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnAllUsersWhenAllScopeAuthorized() throws Exception {
        insertCampus(2L, "第二校区");
        insertInternalUser(2002L, "campus_admin", "13800000011", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(2003L, "self_user", "13800000012", "本人账号", UserDataScopeType.SELF_ONLY);
        bindUserCampus(2002L, 1L, true);
        bindUserCampus(2003L, 2L, true);
        bindUserRole(2002L, 2L);
        bindUserRole(2003L, 2L);

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(6))
                .andExpect(jsonPath("$.data.items.length()").value(6))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[0].roleIds[0]").value(1))
                .andExpect(jsonPath("$.data.items[0].roleIds[1]").value(3))
                .andExpect(jsonPath("$.data.items[0].systemAccount").value(true))
                .andExpect(jsonPath("$.data.items[0].currentUser").value(true))
                .andExpect(jsonPath("$.data.items[1].id").value(1002))
                .andExpect(jsonPath("$.data.items[1].primaryCampusId").value(1))
                .andExpect(jsonPath("$.data.items[1].roleIds[0]").value(2))
                .andExpect(jsonPath("$.data.items[1].systemAccount").value(false))
                .andExpect(jsonPath("$.data.items[1].currentUser").value(false))
                .andExpect(jsonPath("$.data.items[2].id").value(1003))
                .andExpect(jsonPath("$.data.items[3].id").value(1004))
                .andExpect(jsonPath("$.data.items[4].id").value(2002))
                .andExpect(jsonPath("$.data.items[4].primaryCampusId").value(1))
                .andExpect(jsonPath("$.data.items[5].id").value(2003))
                .andExpect(jsonPath("$.data.items[5].primaryCampusId").value(2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldFilterUsersByCampusScopeWhenAuthorized() throws Exception {
        insertCampus(2L, "第二校区");
        insertInternalUser(2002L, "campus_admin", "13800000021", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(2003L, "other_campus", "13800000022", "跨校区账号", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(2002L, 1L, true);
        bindUserCampus(2003L, 2L, true);
        bindUserRole(2002L, 2L);
        bindUserRole(2003L, 2L);

        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        USER_ID,
                        true,
                        1L,
                        UserDataScopeType.SPECIFIED_CAMPUS
                ));
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.items.length()").value(5))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002))
                .andExpect(jsonPath("$.data.items[4].id").value(2002));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnSelfOnlyWhenAuthorized() throws Exception {
        insertInternalUser(2002L, "campus_admin", "13800000031", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(2002L, 1L, true);
        bindUserRole(2002L, 2L);

        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        USER_ID,
                        true,
                        1L,
                        UserDataScopeType.SELF_ONLY
                ));
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1001));
    }

    @Test
    void shouldReturnForbiddenWhenMissingCreateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:user:view"));

        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson("new_admin", "13800000011")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestInvalid() throws Exception {
        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        1002L,
                        true,
                        1L,
                        UserDataScopeType.ALL_COMPANY
                ));
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role", "admin:user:create"));

        String invalidUsername = "invalid user " + System.nanoTime();
        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson(invalidUsername, "13800000012")))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value(anyOf(is(400), is(409))));
    }

    @Test
    void shouldReturnBadRequestWhenCreateDataScopeTypeInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson("new_admin_invalid_scope", "13800000013")
                                .replace("\"dataScopeType\": 4", "\"dataScopeType\": 99")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("数据范围类型无效"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateInternalUserAndRecordAuditContext() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .header("User-Agent", "MockMvc-Test-Agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson("new_admin", "13800000011")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Long createdUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_internal_user WHERE username = ?",
                Long.class,
                "new_admin"
        );
        Integer roleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_role WHERE user_id = ?",
                Integer.class,
                createdUserId
        );
        Integer campusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_campus WHERE user_id = ?",
                Integer.class,
                createdUserId
        );
        Integer mustChangePassword = jdbcTemplate.queryForObject(
                "SELECT must_change_password FROM sys_internal_user WHERE id = ?",
                Integer.class,
                createdUserId
        );

        org.junit.jupiter.api.Assertions.assertEquals(1, roleCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, campusCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, mustChangePassword);

        java.util.Map<String, Object> auditLog = jdbcTemplate.queryForMap(
                """
                SELECT module_name, action_type, biz_id, request_uri, request_method, request_params, status
                FROM sys_audit_log
                WHERE action_type = 'CREATE_INTERNAL_USER'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        org.junit.jupiter.api.Assertions.assertEquals("INTERNAL_USER_MANAGEMENT", auditLog.get("module_name"));
        org.junit.jupiter.api.Assertions.assertEquals("CREATE_INTERNAL_USER", auditLog.get("action_type"));
        org.junit.jupiter.api.Assertions.assertEquals(createdUserId.longValue(), ((Number) auditLog.get("biz_id")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals("/admin/api/v1/internal-users", auditLog.get("request_uri"));
        org.junit.jupiter.api.Assertions.assertEquals("POST", auditLog.get("request_method"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) auditLog.get("status")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(true, ((String) auditLog.get("request_params")).contains("\"username\":\"new_admin\""));
    }

    @Test
    void shouldListCampusOptionsWhenHasCreateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(get("/admin/api/v1/internal-users/campus-options")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("默认校区"));
    }

    @Test
    void shouldReturnForbiddenWhenCampusOptionsMissingCreateAndUpdateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users/campus-options")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnForbiddenWhenMissingUpdateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:user:view"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}", USER_ID)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson("13800000018")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateInternalUserAndClearSecurityCache() throws Exception {
        insertInternalUser(2002L, "update_user", "13800000041", "更新测试用户", UserDataScopeType.ALL_COMPANY);
        bindUserCampus(2002L, 1L, true);
        bindUserRole(2002L, 2L);

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:update"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}", 2002L)
                        .cookie(accessTokenCookie(accessToken))
                        .header("User-Agent", "MockMvc-Test-Agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson("13800000018")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        java.util.Map<String, Object> userRow = jdbcTemplate.queryForMap(
                """
                SELECT phone, name, avatar, data_scope_type
                FROM sys_internal_user
                WHERE id = ?
                """,
                2002L
        );
        org.junit.jupiter.api.Assertions.assertEquals("13800000018", userRow.get("phone"));
        org.junit.jupiter.api.Assertions.assertEquals("更新后管理员", userRow.get("name"));
        org.junit.jupiter.api.Assertions.assertEquals("https://example.com/avatar-updated.png", userRow.get("avatar"));
        org.junit.jupiter.api.Assertions.assertEquals(4, ((Number) userRow.get("data_scope_type")).intValue());

        Integer roleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user_role WHERE user_id = ? AND role_id = 2",
                Integer.class,
                2002L
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, roleCount);

        java.util.Map<String, Object> auditLog = jdbcTemplate.queryForMap(
                """
                SELECT action_type, biz_id, request_uri, request_method, status, request_params
                FROM sys_audit_log
                WHERE action_type = 'UPDATE_INTERNAL_USER'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        org.junit.jupiter.api.Assertions.assertEquals("UPDATE_INTERNAL_USER", auditLog.get("action_type"));
        org.junit.jupiter.api.Assertions.assertEquals(2002L, ((Number) auditLog.get("biz_id")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals("/admin/api/v1/internal-users/2002", auditLog.get("request_uri"));
        org.junit.jupiter.api.Assertions.assertEquals("PUT", auditLog.get("request_method"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) auditLog.get("status")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(true, ((String) auditLog.get("request_params")).contains("\"bizType\":\"INTERNAL_USER_UPDATE\""));

        verify(securityCacheService).clearAuthorityCodes("ADMIN", 2002L);
        verify(securityCacheService).clearUserSnapshot("ADMIN", 2002L);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdateDataScopeTypeInvalid() throws Exception {
        insertInternalUser(2002L, "update_invalid_scope", "13800000042", "更新测试用户", UserDataScopeType.ALL_COMPANY);
        bindUserCampus(2002L, 1L, true);
        bindUserRole(2002L, 2L);

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:update"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}", 2002L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson("13800000019")
                                .replace("\"dataScopeType\": 4", "\"dataScopeType\": 99")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("数据范围类型无效"));
    }

    @Test
    void shouldGetInternalUserDetailWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users/{userId}", USER_ID)
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1001))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.primaryCampusId").value(1))
                .andExpect(jsonPath("$.data.roleIds[0]").value(1))
                .andExpect(jsonPath("$.data.roleIds[1]").value(3))
                .andExpect(jsonPath("$.data.systemAccount").value(true))
                .andExpect(jsonPath("$.data.currentUser").value(true))
                .andExpect(jsonPath("$.data.campusIds[0]").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRejectUpdatingProtectedSystemAdmin() throws Exception {
        insertProtectedSystemAdminUser(2005L, "protected_admin_update", "13800000071", "受保护管理员");

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:update"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}", 2005L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUpdateRequestJson("13800000018")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("系统管理员账号不允许执行修改"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateInternalUserStatusWhenAuthorized() throws Exception {
        insertInternalUser(2002L, "status_user", "13800000051", "状态测试用户", UserDataScopeType.ALL_COMPANY);
        bindUserCampus(2002L, 1L, true);
        bindUserRole(2002L, 2L);

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:enable"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/status", 2002L)
                        .cookie(accessTokenCookie(accessToken))
                        .header("User-Agent", "MockMvc-Test-Agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStatusUpdateRequestJson(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer status = jdbcTemplate.queryForObject(
                "SELECT status FROM sys_internal_user WHERE id = ?",
                Integer.class,
                2002L
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, status);

        java.util.Map<String, Object> auditLog = jdbcTemplate.queryForMap(
                """
                SELECT action_type, biz_id, request_uri, request_method, status
                FROM sys_audit_log
                WHERE action_type = 'UPDATE_INTERNAL_USER_STATUS'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        org.junit.jupiter.api.Assertions.assertEquals("UPDATE_INTERNAL_USER_STATUS", auditLog.get("action_type"));
        org.junit.jupiter.api.Assertions.assertEquals(2002L, ((Number) auditLog.get("biz_id")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals("/admin/api/v1/internal-users/2002/status", auditLog.get("request_uri"));
        org.junit.jupiter.api.Assertions.assertEquals("PUT", auditLog.get("request_method"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) auditLog.get("status")).intValue());

        verify(securityCacheService).clearUserSnapshot("ADMIN", 2002L);
        verify(securityCacheService).clearAuthorityCodes("ADMIN", 2002L);
    }

    @Test
    void shouldReturnBadRequestWhenUpdateStatusValueInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:enable"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/status", USER_ID)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStatusUpdateRequestJson(9)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("状态值无效"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRejectUpdatingSelfStatus() throws Exception {
        insertInternalUser(2003L, "self_status_user", "13800000052", "本人状态账号", UserDataScopeType.SELF_ONLY);
        bindUserCampus(2003L, 1L, true);
        bindUserRole(2003L, 2L);

        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        2003L,
                        true,
                        1L,
                        UserDataScopeType.SELF_ONLY
                ));
        String accessToken = jwtTokenUtils.generateAccessToken(2003L, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", 2003L))
                .thenReturn(List.of("admin:role", "admin:user:enable"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/status", 2003L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStatusUpdateRequestJson(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能在账号管理中操作自己的账号"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRejectUpdatingProtectedSystemAdminStatus() throws Exception {
        insertProtectedSystemAdminUser(2006L, "protected_admin_status", "13800000072", "受保护状态管理员");

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:enable"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/status", 2006L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validStatusUpdateRequestJson(0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("系统管理员账号不允许执行修改状态"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldResetInternalUserPasswordWhenAuthorized() throws Exception {
        insertInternalUser(2002L, "reset_pwd_user", "13800000061", "重置密码测试用户", UserDataScopeType.ALL_COMPANY);
        bindUserCampus(2002L, 1L, true);
        bindUserRole(2002L, 2L);

        String oldPassword = jdbcTemplate.queryForObject(
                "SELECT password FROM sys_internal_user WHERE id = ?",
                String.class,
                2002L
        );

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:reset_password"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/reset-password", 2002L)
                        .cookie(accessTokenCookie(accessToken))
                        .header("User-Agent", "MockMvc-Test-Agent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validResetPasswordRequestJson("人工重置")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        java.util.Map<String, Object> userRow = jdbcTemplate.queryForMap(
                "SELECT password, must_change_password FROM sys_internal_user WHERE id = ?",
                2002L
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) userRow.get("must_change_password")).intValue());
        org.junit.jupiter.api.Assertions.assertEquals(false, oldPassword.equals(userRow.get("password")));

        java.util.Map<String, Object> auditLog = jdbcTemplate.queryForMap(
                """
                SELECT action_type, biz_id, request_uri, request_method, status
                FROM sys_audit_log
                WHERE action_type = 'RESET_INTERNAL_USER_PASSWORD'
                ORDER BY id DESC
                LIMIT 1
                """
        );
        org.junit.jupiter.api.Assertions.assertEquals("RESET_INTERNAL_USER_PASSWORD", auditLog.get("action_type"));
        org.junit.jupiter.api.Assertions.assertEquals(2002L, ((Number) auditLog.get("biz_id")).longValue());
        org.junit.jupiter.api.Assertions.assertEquals("/admin/api/v1/internal-users/2002/reset-password", auditLog.get("request_uri"));
        org.junit.jupiter.api.Assertions.assertEquals("PUT", auditLog.get("request_method"));
        org.junit.jupiter.api.Assertions.assertEquals(1, ((Number) auditLog.get("status")).intValue());

        verify(securityCacheService).clearUserSnapshot("ADMIN", 2002L);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRejectResettingOwnPassword() throws Exception {
        insertInternalUser(2004L, "self_reset_user", "13800000062", "本人重置账号", UserDataScopeType.SELF_ONLY);
        bindUserCampus(2004L, 1L, true);
        bindUserRole(2004L, 2L);

        when(securityCacheService.getUserSnapshot(eq("ADMIN"), anyLong()))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        2004L,
                        true,
                        1L,
                        UserDataScopeType.SELF_ONLY
                ));
        String accessToken = jwtTokenUtils.generateAccessToken(2004L, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", 2004L))
                .thenReturn(List.of("admin:role", "admin:user:reset_password"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/reset-password", 2004L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validResetPasswordRequestJson("人工重置")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不能在账号管理中操作自己的账号"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldRejectResettingProtectedSystemAdminPassword() throws Exception {
        insertProtectedSystemAdminUser(2007L, "protected_admin_reset", "13800000073", "受保护重置管理员");

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:reset_password"));

        mockMvc.perform(put("/admin/api/v1/internal-users/{userId}/reset-password", 2007L)
                        .cookie(accessTokenCookie(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validResetPasswordRequestJson("人工重置")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("系统管理员账号不允许执行重置密码"));
    }

    private String validCreateRequestJson(String username, String phone) {
        return """
                {
                  "username": "%s",
                  "phone": "%s",
                  "name": "新教务",
                  "avatar": "https://example.com/avatar.png",
                  "roleIds": [1],
                  "campusIds": [1],
                  "primaryCampusId": 1,
                  "dataScopeType": 4
                }
                """.formatted(username, phone);
    }

    private String validUpdateRequestJson(String phone) {
        return """
                {
                  "phone": "%s",
                  "name": "更新后管理员",
                  "avatar": "https://example.com/avatar-updated.png",
                  "roleIds": [2],
                  "campusIds": [1],
                  "primaryCampusId": 1,
                  "dataScopeType": 4
                }
                """.formatted(phone);
    }

    private String validStatusUpdateRequestJson(int status) {
        return """
                {
                  "status": %d
                }
                """.formatted(status);
    }

    private String validResetPasswordRequestJson(String reason) {
        return """
                {
                  "reason": "%s"
                }
                """.formatted(reason);
    }

    private Cookie accessTokenCookie(String accessToken) {
        return new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken);
    }

    private void insertCampus(Long campusId, String campusName) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_campus (id, name, address, phone, leader_id, leader_name, sort, status, remark, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                campusId, campusName, "杭州", "0571-00000001", null, null, 2, 1, "测试校区", NOW, NOW
        );
    }

    private void insertInternalUser(
            Long userId,
            String username,
            String phone,
            String name,
            UserDataScopeType dataScopeType
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                userId,
                username,
                "$2a$10$5vIb8UziDCbwzXwGXWkA2uyZWG.Pa9QrCrIKU8iS3TznhUFCBaDDW",
                phone,
                name,
                null,
                1,
                0,
                dataScopeType.getCode(),
                NOW,
                NOW
        );
    }

    private void insertProtectedSystemAdminUser(Long userId, String username, String phone, String name) {
        insertInternalUser(userId, username, phone, name, UserDataScopeType.ALL_COMPANY);
        bindUserCampus(userId, 1L, true);
        bindUserRole(userId, 3L);
    }

    private void bindUserCampus(Long userId, Long campusId, boolean primary) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_user_campus (user_id, campus_id, is_primary, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                userId,
                campusId,
                primary ? 1 : 0,
                NOW,
                NOW
        );
    }

    private void bindUserRole(Long userId, Long roleId) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_user_role (user_id, role_id, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                """,
                userId,
                roleId,
                NOW,
                NOW
        );
    }
}
