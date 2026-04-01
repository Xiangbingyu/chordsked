package com.chordsked.backend.controller;

import com.chordsked.backend.cache.AuthLoginCacheService;
import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.dao.mapper.InternalLoginMapper;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_auth_login_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class AuthControllerIntegrationTest {
    private static final String USERNAME = "admin";
    private static final String RAW_PASSWORD = "Aa123456!";
    private static final Long USER_ID = 1001L;


    @TestConfiguration
    static class AuthControllerTestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "internalLoginMapper")
    private InternalLoginMapper internalLoginMapper;

    @MockitoBean(name = "authLoginCacheService")
    private AuthLoginCacheService authLoginCacheService;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Test
    void shouldLoginSuccessfullyAndSetTokenCookies() throws Exception {
        InternalUserEntity internalUser = createEnabledUser(null);
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalLoginMapper.selectInternalUserByUsername(USERNAME)).thenReturn(internalUser);
        when(internalLoginMapper.resetInternalUserLoginFail(eq(USER_ID), any(LocalDateTime.class))).thenReturn(1);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(1001))
                .andExpect(jsonPath("$.data.userType").value("ADMIN"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("access_token=")));
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("refresh_token=")));
                });
        verify(internalLoginMapper, times(1)).resetInternalUserLoginFail(eq(USER_ID), any(LocalDateTime.class));
        verify(internalLoginMapper, never()).increaseInternalUserLoginFail(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void shouldRejectWhenPasswordWrongAndIncreaseFailCount() throws Exception {
        InternalUserEntity internalUser = createEnabledUser(null);
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalLoginMapper.selectInternalUserByUsername(USERNAME)).thenReturn(internalUser);
        when(internalLoginMapper.increaseInternalUserLoginFail(eq(USER_ID), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"WrongPassword1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));

        verify(internalLoginMapper, times(1)).increaseInternalUserLoginFail(eq(USER_ID), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(authLoginCacheService, times(1)).evictInternalUserLoginSnapshot(USERNAME);
        verify(internalLoginMapper, never()).resetInternalUserLoginFail(any(Long.class), any(LocalDateTime.class));
    }

    @Test
    void shouldRejectWhenAccountLocked() throws Exception {
        InternalUserEntity internalUser = createEnabledUser(LocalDateTime.now().plusMinutes(10));
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalLoginMapper.selectInternalUserByUsername(USERNAME)).thenReturn(internalUser);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已锁定，请 30 分钟后重试"));

        verify(internalLoginMapper, never()).increaseInternalUserLoginFail(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(internalLoginMapper, never()).resetInternalUserLoginFail(any(Long.class), any(LocalDateTime.class));
    }

    private InternalUserEntity createEnabledUser(LocalDateTime lockedUntil) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(USER_ID);
        internalUser.setUsername(USERNAME);
        internalUser.setPassword(encoder.encode(RAW_PASSWORD));
        internalUser.setStatus(1);
        internalUser.setMustChangePassword(0);
        internalUser.setLoginFailCount(0);
        internalUser.setLockedUntil(lockedUntil);
        internalUser.setDataScopeType(1);
        internalUser.setName("系统管理员");
        return internalUser;
    }
}
