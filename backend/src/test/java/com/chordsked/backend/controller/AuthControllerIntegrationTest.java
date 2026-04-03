package com.chordsked.backend.controller;

import com.chordsked.backend.cache.InternalAuthLoginCacheService;
import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.dao.InternalUserDao;
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
        "spring.datasource.url=jdbc:h2:mem:chordsked_auth_login_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "chordsked.security.internal-auth.login.fail-lock-threshold=7",
        "chordsked.security.internal-auth.login.fail-lock-minutes=45"
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

    @MockitoBean(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @MockitoBean(name = "internalAuthLoginCacheService")
    private InternalAuthLoginCacheService authLoginCacheService;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Test
    void shouldLoginSuccessfullyAndSetTokenCookies() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginCacheService.getLockedUntil(USERNAME)).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(1001))
                .andExpect(jsonPath("$.data.userType").value("ADMIN"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("access_token=") && cookie.contains("HttpOnly")));
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("refresh_token=") && cookie.contains("HttpOnly")));
                });
        verify(authLoginCacheService, times(1)).clearLoginState(USERNAME);
        verify(securityCacheService, times(2)).markTokenActive(any(), any());
    }

    @Test
    void shouldRejectWhenPasswordWrongAndIncreaseFailCount() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginCacheService.getLockedUntil(USERNAME)).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"WrongPassword1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));

        verify(authLoginCacheService, times(1)).recordLoginFailure(eq(USERNAME), eq(7), eq(45L));
        verify(authLoginCacheService, never()).clearLoginState(USERNAME);
    }

    @Test
    void shouldRejectWhenAccountLocked() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginCacheService.getLockedUntil(USERNAME)).thenReturn(System.currentTimeMillis() + 10 * 60 * 1000L);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已锁定，请 45 分钟后重试"));

        verify(authLoginCacheService, never()).recordLoginFailure(eq(USERNAME), eq(7), eq(45L));
        verify(authLoginCacheService, never()).clearLoginState(USERNAME);
    }

    @Test
    void shouldRejectWhenUserDisabled() throws Exception {
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(createDisabledUser());

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已被禁用，请联系管理员"));

        verify(authLoginCacheService, never()).recordLoginFailure(eq(USERNAME), eq(7), eq(45L));
    }

    @Test
    void shouldRejectWhenUserNotFound() throws Exception {
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));
    }

    @Test
    void shouldReturnBadRequestWhenPasswordBlank() throws Exception {
        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenUsernameContainsWhitespace() throws Exception {
        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin test\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenUsernameTooLong() throws Exception {
        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldUseSnapshotWhenLoginCacheHit() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(authLoginCacheService.getInternalUserLoginSnapshot(USERNAME)).thenReturn(internalUser);
        when(authLoginCacheService.getLockedUntil(USERNAME)).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(internalUserDao, never()).getByUsername(USERNAME);
    }

    private InternalUserEntity createEnabledUser() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(USER_ID);
        internalUser.setUsername(USERNAME);
        internalUser.setPassword(encoder.encode(RAW_PASSWORD));
        internalUser.setStatus(1);
        internalUser.setMustChangePassword(0);
        internalUser.setDataScopeType(1);
        internalUser.setName("系统管理员");
        return internalUser;
    }

    private InternalUserEntity createDisabledUser() {
        InternalUserEntity internalUser = createEnabledUser();
        internalUser.setStatus(0);
        return internalUser;
    }
}
