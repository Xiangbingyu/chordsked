package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_controller_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgControllerIntegrationTest {
    private static final Long USER_ID = 1001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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
                        1L,
                        UserDataScopeType.ALL
                ));
    }

    @Test
    void shouldListOrgNodeOptionsWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:user:create"));

        mockMvc.perform(get("/admin/api/v1/org-nodes/options")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("默认校区"));
    }

    @Test
    void shouldListOrgTreeWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:view"));

        mockMvc.perform(get("/admin/api/v1/org-tree")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateDeptNodeWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:create"));

        mockMvc.perform(post("/admin/api/v1/org-nodes")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId": 1,
                                  "nodeType": 2,
                                  "code": "DEPT-JW",
                                  "name": "教务部",
                                  "sort": 1,
                                  "status": 1,
                                  "remark": "测试创建"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateOrgNodeWhenAuthorized() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (100, 1, 2, 'DEPT-OLD', '旧部门', 1, '1', 2, 1, 1, '测试更新', 1774483200000, 1774483200000)
                """
        );
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:update"));

        mockMvc.perform(put("/admin/api/v1/org-nodes/100")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "DEPT-NEW",
                                  "name": "新部门",
                                  "sort": 2,
                                  "status": 1,
                                  "remark": "更新后"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteOrgNodeWhenAuthorized() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (101, 1, 2, 'DEPT-DELETE', '待删部门', 1, '1', 2, 1, 1, '测试删除', 1774483200000, 1774483200000)
                """
        );
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:delete"));

        mockMvc.perform(delete("/admin/api/v1/org-nodes/101")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldListOrgAccountsOptionsWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:assign_user"));

        mockMvc.perform(get("/admin/api/v1/org-accounts/options")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].userId").value(1002));
    }

    @Test
    void shouldListNodeBoundUsersWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:view"));

        mockMvc.perform(get("/admin/api/v1/org-nodes/1/users")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateNodeBindingsWhenAuthorized() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (102, 1, 2, 'DEPT-BIND', '绑定部门', 1, '1', 2, 1, 1, '测试绑定', 1774483200000, 1774483200000)
                """
        );
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:assign_user"));

        mockMvc.perform(put("/admin/api/v1/org-nodes/102/users")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userIds": [1002]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldKeepOtherOrgBindingsWhenUpdatingSingleNode() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (103, 1, 2, 'DEPT-A', '部门A', 1, '1', 2, 1, 1, '测试部门A', 1774483200000, 1774483200000),
                       (104, 1, 2, 'DEPT-B', '部门B', 1, '1', 2, 2, 1, '测试部门B', 1774483200000, 1774483200000)
                """
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, campus_id, org_node_id, created_at, updated_at)
                VALUES (2001, 'multi_scope_user', 'pwd', '13800009999', '多组织账号', NULL, 1, 0, 2, 1, 103, 1774483200000, 1774483200000)
                """
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_user_org_scope (user_id, org_node_id, is_primary, created_at, updated_at)
                VALUES (2001, 103, 1, 1774483200000, 1774483200000),
                       (2001, 104, 0, 1774483200000, 1774483200000)
                """
        );

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:assign_user"));

        mockMvc.perform(put("/admin/api/v1/org-nodes/104/users")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userIds": [2001]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer scopeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_org_scope WHERE user_id = 2001",
                Integer.class
        );
        Integer deptAScopeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_org_scope WHERE user_id = 2001 AND org_node_id = 103",
                Integer.class
        );
        Integer deptBScopeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_org_scope WHERE user_id = 2001 AND org_node_id = 104",
                Integer.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(2, scopeCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, deptAScopeCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, deptBScopeCount);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldInitializePrimaryOrgWhenBindingUserWithoutPrimaryOrg() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (105, 1, 2, 'DEPT-C', '部门C', 1, '1', 2, 3, 1, '测试部门C', 1774483200000, 1774483200000)
                """
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, campus_id, org_node_id, created_at, updated_at)
                VALUES (2002, 'tree_bind_user', 'pwd', '13800008888', '树绑定账号', NULL, 1, 0, 2, NULL, NULL, 1774483200000, 1774483200000)
                """
        );

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:assign_user"));

        mockMvc.perform(put("/admin/api/v1/org-nodes/105/users")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userIds": [2002]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Long primaryOrgNodeId = jdbcTemplate.queryForObject(
                "SELECT org_node_id FROM sys_internal_user WHERE id = 2002",
                Long.class
        );
        Integer primaryScopeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_org_scope WHERE user_id = 2002 AND org_node_id = 105 AND is_primary = 1",
                Integer.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(105L, primaryOrgNodeId);
        org.junit.jupiter.api.Assertions.assertEquals(1, primaryScopeCount);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldExcludeAssignedUsersWithoutPrimaryOrgFromOrgAccountOptions() throws Exception {
        jdbcTemplate.update(
                """
                INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, campus_id, org_node_id, created_at, updated_at)
                VALUES (2003, 'invalid_scope_user', 'pwd', '13800007777', '非法授权账号', NULL, 1, 0, 2, 1, NULL, 1774483200000, 1774483200000)
                """
        );

        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:org:assign_user"));

        mockMvc.perform(get("/admin/api/v1/org-accounts/options")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[?(@.userId==2003)]").isEmpty());
    }
}
