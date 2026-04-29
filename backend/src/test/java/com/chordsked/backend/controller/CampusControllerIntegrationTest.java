package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_controller_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "chordsked.datascope.enabled=false"
})
class CampusControllerIntegrationTest {
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
        mockMvc.perform(get("/admin/api/v1/campuses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/admin/api/v1/campuses")
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS-TEST\",\"name\":\"测试校区\",\"status\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/admin/api/v1/campuses/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(put("/admin/api/v1/campuses/1")
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS-TEST\",\"name\":\"测试校区\",\"status\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(delete("/admin/api/v1/campuses/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnForbiddenWhenMissingCampusViewAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role", "admin:campus:menu"));

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(get("/admin/api/v1/campuses/1")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnCampusListWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(5))
                .andExpect(jsonPath("$.data.items.length()").value(5))
                .andExpect(jsonPath("$.data.items[0].code").value("CAMPUS-DEFAULT"))
                .andExpect(jsonPath("$.data.items[0].name").value("默认校区"));
    }

    @Test
    void shouldFilterByKeyword() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .param("keyword", "西湖"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("西湖校区"));
    }

    @Test
    void shouldReturnBadRequestWhenPageInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnForbiddenWhenMissingCampusCreateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(post("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS-NEW\",\"name\":\"新校区\",\"status\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateCampusWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view", "admin:campus:create"));

        mockMvc.perform(post("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS-NEW\",\"name\":\"新校区\",\"address\":\"测试地址\",\"phone\":\"010-12345678\",\"status\":1,\"remark\":\"测试备注\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(6));
    }

    @Test
    void shouldReturnBadRequestWhenCreateCampusCodeBlank() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:create"));

        mockMvc.perform(post("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType("application/json")
                        .content("{\"code\":\"\",\"name\":\"新校区\",\"status\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnCampusDetailWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(get("/admin/api/v1/campuses/2")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.code").value("CAMPUS-XH"))
                .andExpect(jsonPath("$.data.name").value("西湖校区"))
                .andExpect(jsonPath("$.data.address").value("杭州西湖区"))
                .andExpect(jsonPath("$.data.phone").value("0571-00000001"))
                .andExpect(jsonPath("$.data.leaderId").value(1001))
                .andExpect(jsonPath("$.data.leaderName").value("系统管理员"));
    }

    @Test
    void shouldReturnBadRequestWhenCampusDetailIdInvalid() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(get("/admin/api/v1/campuses/0")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnForbiddenWhenMissingCampusUpdateAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(put("/admin/api/v1/campuses/2")
                        .cookie(new Cookie("access_token", accessToken))
                        .contentType("application/json")
                        .content("{\"code\":\"CAMPUS-XH\",\"name\":\"西湖校区\",\"status\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnForbiddenWhenMissingCampusDeleteAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view"));

        mockMvc.perform(delete("/admin/api/v1/campuses/5")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnBadRequestWhenDeletingCampusWithUserBinding() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:delete"));

        mockMvc.perform(delete("/admin/api/v1/campuses/1")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("校区有教务账号绑定，请先移除用户"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteCampusWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:campus:menu", "admin:campus:view", "admin:campus:delete"));

        mockMvc.perform(delete("/admin/api/v1/campuses/5")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/admin/api/v1/campuses")
                        .cookie(new Cookie("access_token", accessToken))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(4));
    }
}
