package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnAllUsersWhenAllScopeAuthorized() throws Exception {
        insertCampus(2L, "第二校区");
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(1003L, "self_user", "13800000002", "本人账号", UserDataScopeType.SELF_ONLY);
        bindUserCampus(1002L, 1L, true);
        bindUserCampus(1003L, 2L, true);
        bindUserRole(1002L, 2L);
        bindUserRole(1003L, 2L);

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:view"));

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[0].roleIds[0]").value(1))
                .andExpect(jsonPath("$.data.items[1].id").value(1002))
                .andExpect(jsonPath("$.data.items[1].primaryCampusId").value(1))
                .andExpect(jsonPath("$.data.items[1].roleIds[0]").value(2))
                .andExpect(jsonPath("$.data.items[2].id").value(1003))
                .andExpect(jsonPath("$.data.items[2].primaryCampusId").value(2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldFilterUsersByCampusScopeWhenAuthorized() throws Exception {
        insertCampus(2L, "第二校区");
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(1003L, "other_campus", "13800000002", "跨校区账号", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(1002L, 1L, true);
        bindUserCampus(1003L, 2L, true);
        bindUserRole(1002L, 2L);
        bindUserRole(1003L, 2L);

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
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnSelfOnlyWhenAuthorized() throws Exception {
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(1002L, 1L, true);
        bindUserRole(1002L, 2L);

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
                        .header("Authorization", "Bearer " + accessToken))
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
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson("new_admin", "13800000011")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateRequestJson("invalid user", "13800000012")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateInternalUserAndRecordAuditContext() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(post("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken)
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
