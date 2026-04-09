package com.chordsked.backend.controller;

import com.chordsked.backend.cache.auth.AuthLoginStateCacheService;
import com.chordsked.backend.cache.auth.provider.AuthLoginSnapshotCacheProvider;
import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.service.verification.userstatus.UserStatusVerificationService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
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
        "chordsked.security.auth.login.fail-lock-threshold=7",
        "chordsked.security.auth.login.fail-lock-minutes=45"
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

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @MockitoBean(name = "authLoginStateCacheService")
    private AuthLoginStateCacheService authLoginStateCacheService;

    @MockitoBean(name = "usernamePasswordLoginSnapshotCacheProvider")
    private AuthLoginSnapshotCacheProvider usernamePasswordLoginSnapshotCacheProvider;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @MockitoBean(name = "userStatusVerificationService")
    private UserStatusVerificationService userStatusVerificationService;

    @BeforeEach
    void setUp() {
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginMethod()).thenReturn(AuthLoginMethod.USERNAME_PASSWORD);
    }

    @Test
    void shouldLoginSuccessfullyAndSetTokenCookies() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginStateCacheService.getLockedUntil(any(), eq(USERNAME))).thenReturn(null);

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
        verify(authLoginStateCacheService, times(1)).clearLoginState(any(), eq(USERNAME));
        verify(securityCacheService, times(2)).markTokenActive(any(), any());
    }

    @Test
    void shouldRejectWhenPasswordWrongAndIncreaseFailCount() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginStateCacheService.getLockedUntil(any(), eq(USERNAME))).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"WrongPassword1!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号或密码错误"));

        verify(authLoginStateCacheService, times(1)).recordLoginFailure(any(), eq(USERNAME), eq(7), eq(45L));
        verify(authLoginStateCacheService, never()).clearLoginState(any(), eq(USERNAME));
    }

    @Test
    void shouldRejectWhenAccountLocked() throws Exception {
        InternalUserEntity internalUser = createEnabledUser();
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(internalUser);
        when(authLoginStateCacheService.getLockedUntil(any(), eq(USERNAME))).thenReturn(System.currentTimeMillis() + 10 * 60 * 1000L);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已锁定，请 45 分钟后重试"));

        verify(authLoginStateCacheService, never()).recordLoginFailure(any(), eq(USERNAME), eq(7), eq(45L));
        verify(authLoginStateCacheService, never()).clearLoginState(any(), eq(USERNAME));
    }

    @Test
    void shouldRejectWhenUserDisabled() throws Exception {
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME)).thenReturn(null);
        when(internalUserDao.getByUsername(USERNAME)).thenReturn(createDisabledUser());

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("账号已被禁用，请联系管理员"));

        verify(authLoginStateCacheService, never()).recordLoginFailure(any(), eq(USERNAME), eq(7), eq(45L));
    }

    @Test
    void shouldRejectWhenUserNotFound() throws Exception {
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME)).thenReturn(null);
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
                .andExpect(status().isConflict())
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
    void shouldRejectWhenUsernamePasswordMethodFieldsMissing() throws Exception {
        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"loginMethod\":\"USERNAME_PASSWORD\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名和密码不能为空"));
    }

    @Test
    void shouldRejectWhenPhoneSmsCodeMethodFieldsMissing() throws Exception {
        mockMvc.perform(post("/students/api/v1/login")
                        .contentType("application/json")
                        .content("{\"loginMethod\":\"PHONE_SMS_CODE\",\"smsCode\":\"123456\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("手机号和短信验证码不能为空"));
    }

    @Test
    void shouldRejectWhenLoginMethodCannotBeResolvedAtController() throws Exception {
        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("登录请求参数不合法"));
    }

    @Test
    void shouldUseSnapshotWhenLoginCacheHit() throws Exception {
        when(usernamePasswordLoginSnapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, USERNAME))
                .thenReturn(createUsernamePasswordSnapshot());
        when(authLoginStateCacheService.getLockedUntil(any(), eq(USERNAME))).thenReturn(null);

        mockMvc.perform(post("/admin/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(internalUserDao, never()).getByUsername(USERNAME);
    }

    @Test
    void shouldRejectWhenTeacherUsernamePasswordLoginNotSupported() throws Exception {
        mockMvc.perform(post("/teachers/api/v1/login")
                        .contentType("application/json")
                        .content("{\"username\":\"teacher01\",\"password\":\"Aa123456!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前账号类型暂不支持该登录方式"));
    }

    @Test
    void shouldRejectStudentPhoneSmsCodeLoginWhenMethodNotOpen() throws Exception {
        mockMvc.perform(post("/students/api/v1/login")
                        .contentType("application/json")
                        .content("{\"phone\":\"13800138000\",\"smsCode\":\"123456\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("手机号验证码登录暂未开放"));
    }

    @Test
    void shouldRefreshSuccessfullyAndSetNewTokenCookies() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, AccountUserType.ADMIN.getCode());
        String refreshToken = jwtTokenUtils.generateRefreshToken(USER_ID, AccountUserType.ADMIN.getCode());
        when(securityCacheService.isTokenActive(eq(refreshToken))).thenReturn(true);
        when(securityCacheService.isTokenRevoked(eq(refreshToken))).thenReturn(false);

        mockMvc.perform(post("/admin/api/v1/refresh")
                        .cookie(
                                new Cookie("access_token", accessToken),
                                new Cookie("refresh_token", refreshToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(1001))
                .andExpect(jsonPath("$.data.userType").value("ADMIN"))
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("access_token=") && cookie.contains("HttpOnly")));
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("refresh_token=") && cookie.contains("HttpOnly")));
                });

        verify(userStatusVerificationService, times(1))
                .verify(eq(USER_ID), eq(AccountUserType.ADMIN));
        verify(securityCacheService, times(2)).markTokenRevoked(any(), any());
        verify(securityCacheService, times(2)).markTokenActive(any(), any());
    }

    @Test
    void shouldRejectRefreshWhenTokenUserTypeDoesNotMatchRoute() throws Exception {
        String refreshToken = jwtTokenUtils.generateRefreshToken(USER_ID, AccountUserType.TEACHER.getCode());
        when(securityCacheService.isTokenActive(eq(refreshToken))).thenReturn(true);
        when(securityCacheService.isTokenRevoked(eq(refreshToken))).thenReturn(false);

        mockMvc.perform(post("/admin/api/v1/refresh")
                        .cookie(new Cookie("refresh_token", refreshToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("登录用户类型与访问端不匹配"));

        verify(userStatusVerificationService, never()).verify(any(), any());
    }

    @Test
    void shouldLogoutSuccessfullyAndClearTokenCookies() throws Exception {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, AccountUserType.ADMIN.getCode());
        String refreshToken = jwtTokenUtils.generateRefreshToken(USER_ID, AccountUserType.ADMIN.getCode());

        mockMvc.perform(post("/admin/api/v1/logout")
                        .cookie(
                                new Cookie("access_token", accessToken),
                                new Cookie("refresh_token", refreshToken)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("access_token=") && cookie.contains("Max-Age=0")));
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("refresh_token=") && cookie.contains("Max-Age=0")));
                });

        verify(securityCacheService, times(2)).markTokenRevoked(any(), any());
    }

    @Test
    void shouldLogoutSuccessfullyWhenTokenCookiesMissing() throws Exception {
        mockMvc.perform(post("/admin/api/v1/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(result -> {
                    List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("access_token=") && cookie.contains("Max-Age=0")));
                    assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("refresh_token=") && cookie.contains("Max-Age=0")));
                });

        verify(securityCacheService, never()).markTokenRevoked(any(), any());
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

    private UsernamePasswordLoginSnapshot createUsernamePasswordSnapshot() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        UsernamePasswordLoginSnapshot snapshot = new UsernamePasswordLoginSnapshot();
        snapshot.setUserId(USER_ID);
        snapshot.setUserType(AccountUserType.ADMIN);
        snapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
        snapshot.setPrincipal(USERNAME);
        snapshot.setPassword(encoder.encode(RAW_PASSWORD));
        snapshot.setEnabled(true);
        snapshot.setMustChangePassword(false);
        snapshot.setName("系统管理员");
        return snapshot;
    }
}
