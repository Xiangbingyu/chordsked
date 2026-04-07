package com.chordsked.backend.security;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.StudentUserStatus;
import com.chordsked.backend.model.vo.student.StudentListResultVO;
import com.chordsked.backend.service.student.StudentListService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @TestConfiguration
    static class SecurityTestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean(name = "studentListService")
    private StudentListService studentListService;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @BeforeEach
    void setupSecurityRedisMock() {
        when(securityCacheService.isTokenRevoked(anyString())).thenReturn(false);
        when(securityCacheService.isTokenActive(anyString())).thenReturn(false);
        when(securityCacheService.getAuthorityCodes(eq("ADMIN"), anyLong())).thenReturn(List.of("admin:role"));
        when(securityCacheService.getAuthorityCodes(eq("TEACHER"), anyLong())).thenReturn(List.of("teacher:role"));
        when(securityCacheService.getAuthorityCodes(eq("STUDENT"), anyLong())).thenReturn(List.of("student:role"));
    }

    @Test
    void shouldGenerateAndParseTokenClaims() {
        String accessToken = jwtTokenUtils.generateAccessToken(1001L, "admin");
        String refreshToken = jwtTokenUtils.generateRefreshToken(1001L, "admin");
        Claims accessClaims = jwtTokenUtils.parseClaims(accessToken);
        Claims refreshClaims = jwtTokenUtils.parseClaims(refreshToken);
        assertEquals(1001L, accessClaims.get("userId", Long.class));
        assertEquals("ADMIN", accessClaims.get("userType", String.class));
        assertEquals("access", accessClaims.get("tokenType", String.class));
        assertEquals("refresh", refreshClaims.get("tokenType", String.class));
        assertTrue(accessClaims.getExpiration().after(new java.util.Date()));
        assertTrue(refreshClaims.getExpiration().after(new java.util.Date()));
        assertTrue(jwtTokenUtils.isAccessTokenValid(accessToken));
        assertTrue(jwtTokenUtils.isRefreshTokenValid(refreshToken));
        assertFalse(jwtTokenUtils.isAccessTokenValid(refreshToken));
        assertFalse(jwtTokenUtils.isRefreshTokenValid(accessToken));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .queryParam("keyword", "张")
                        .queryParam("status", "1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldProtectBaseApiPrefixWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/students/api/v1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenTypeIsRefresh() throws Exception {
        String refreshToken = jwtTokenUtils.generateRefreshToken(1001L, "ADMIN");
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenExpired() throws Exception {
        String expiredToken = jwtTokenUtils.generateToken(1001L, "ADMIN", -1L, "access");
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenMalformed() throws Exception {
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .header("Authorization", "Bearer malformed-jwt-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnUnauthorizedWhenUserTypeUnsupported() throws Exception {
        String token = jwtTokenUtils.generateAccessToken(1001L, "UNKNOWN");
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnForbiddenWhenRoleDoesNotMatchApiPrefix() throws Exception {
        String teacherToken = jwtTokenUtils.generateAccessToken(1001L, "TEACHER");
        when(securityCacheService.isTokenActive(eq(teacherToken))).thenReturn(true);
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldPassSecurityWhenAccessTokenIsValid() throws Exception {
        when(studentListService.list(org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResult.of(1, List.of(new StudentListResultVO(1L, "13700000000", "张小明", StudentUserStatus.ENABLED, 1L))));

        String accessToken = jwtTokenUtils.generateAccessToken(1001L, "STUDENT");
        when(securityCacheService.isTokenActive(eq(accessToken))).thenReturn(true);
        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .queryParam("keyword", "张")
                        .queryParam("status", "1")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("张小明"));
    }
}
