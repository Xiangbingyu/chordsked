package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_permission_controller_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class PermissionControllerIntegrationTest {
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
        mockMvc.perform(get("/admin/api/v1/permissions/tree"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnForbiddenWhenMissingFineGrainedAuthority() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID)).thenReturn(List.of("admin:role"));

        mockMvc.perform(get("/admin/api/v1/permissions/tree")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldReturnFullPermissionTreeWhenAuthorized() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, "ADMIN");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        when(securityCacheService.getAuthorityCodes("ADMIN", USER_ID))
                .thenReturn(List.of("admin:role", "admin:role:view"));

        mockMvc.perform(get("/admin/api/v1/permissions/tree")
                        .cookie(new Cookie("access_token", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].code").value("admin:role"))
                .andExpect(jsonPath("$.data[0].children.length()").value(0))
                .andExpect(jsonPath("$.data[1].code").value("admin:auth:menu"))
                .andExpect(jsonPath("$.data[1].children[0].code").value("admin:auth:logout"))
                .andExpect(jsonPath("$.data[2].code").value("admin:user:menu"))
                .andExpect(jsonPath("$.data[2].children.length()").value(6))
                .andExpect(jsonPath("$.data[2].children[5].code").value("admin:user:reset_password"))
                .andExpect(jsonPath("$.data[3].code").value("admin:role:menu"))
                .andExpect(jsonPath("$.data[3].children.length()").value(5))
                .andExpect(jsonPath("$.data[3].children[4].code").value("admin:role:assign_permission"))
                .andExpect(jsonPath("$.data[4].code").value("admin:org:menu"))
                .andExpect(jsonPath("$.data[4].children.length()").value(5))
                .andExpect(jsonPath("$.data[5].code").value("admin:log:menu"))
                .andExpect(jsonPath("$.data[5].children.length()").value(4));
    }
}
