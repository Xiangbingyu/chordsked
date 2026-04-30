package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.utils.cookie.CookieUtils;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_internal_user_infra_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.port=6380",
        "spring.data.redis.database=15",
        "spring.data.redis.timeout=5s",
        "chordsked.security.redis.enabled=true",
        "chordsked.security.redis.token-session-enabled=true",
        "chordsked.security.redis.authority-cache-ttl-seconds=300",
        "chordsked.security.redis.user-snapshot-cache-ttl-seconds=300"
})
class InternalUserControllerInfrastructureIntegrationTest {
    private static final Long USER_ID = 1001L;
    private static final long NOW = 1774483200000L;
    private static final String USER_TYPE = "ADMIN";
    private static final String USER_SNAPSHOT_KEY = "chordsked:security:user:ADMIN:1001";
    private static final String AUTHORITY_KEY = "chordsked:security:authority:ADMIN:1001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SecurityCacheService securityCacheService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        clearRedis();
    }

    @AfterEach
    void tearDown() {
        clearRedis();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldLoadAuthorityAndSnapshotFromH2AndCacheIntoRedis() throws Exception {
        insertCampus(12L, "第二校区");
        insertInternalUser(2102L, "org_admin_a", "13800001001", "组织管理员", UserDataScopeType.ASSIGNED, 1L, 1L);
        insertInternalUser(2103L, "self_user_a", "13800001002", "本人账号", UserDataScopeType.SELF, 12L, 12L);
        bindUserOrgScope(2102L, 1L, true);
        bindUserRole(2102L, 2L);
        bindUserRole(2103L, 2L);

        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(6))
                .andExpect(jsonPath("$.data.items.length()").value(6))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002))
                .andExpect(jsonPath("$.data.items[2].id").value(1003));

        SecurityCacheService.SecurityUserSnapshot snapshot = securityCacheService.getUserSnapshot(USER_TYPE, USER_ID);
        assertNotNull(snapshot);
        assertEquals(UserDataScopeType.ALL, snapshot.dataScopeType());
        assertEquals(1L, snapshot.currentCampusId());
        assertEquals(1L, snapshot.primaryOrgNodeId());
        assertTrue(securityCacheService.getAuthorityCodes(USER_TYPE, USER_ID).contains("admin:user:view"));
        assertEquals("1|1|1|1", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
        assertTrue(stringRedisTemplate.opsForValue().get(AUTHORITY_KEY).contains("admin:user:view"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUseRedisSnapshotForCampusScopeAndTriggerDataScopeFilter() throws Exception {
        insertCampus(12L, "第二校区");
        insertInternalUser(2102L, "org_admin_b", "13800001011", "组织管理员", UserDataScopeType.ASSIGNED, 1L, 1L);
        insertInternalUser(2103L, "other_campus_b", "13800001012", "跨校区账号", UserDataScopeType.ASSIGNED, 12L, 12L);
        bindUserOrgScope(USER_ID, 1L, true);
        bindUserOrgScope(2102L, 1L, true);
        bindUserOrgScope(2103L, 12L, true);
        bindUserRole(2102L, 2L);
        bindUserRole(2103L, 2L);
        securityCacheService.cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        USER_TYPE,
                        USER_ID,
                        true,
                        1L,
                        1L,
                        UserDataScopeType.ASSIGNED
                )
        );
        securityCacheService.cacheAuthorityCodes(USER_TYPE, USER_ID, List.of("admin:role", "admin:user:view"));
        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.items.length()").value(4))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002));

        assertEquals("1|1|1|2", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUseRedisSnapshotForSelfScopeAndTriggerDataScopeFilter() throws Exception {
        insertInternalUser(2102L, "org_admin_c", "13800001021", "组织管理员", UserDataScopeType.ASSIGNED, 1L, 1L);
        bindUserOrgScope(2102L, 1L, true);
        bindUserRole(2102L, 2L);
        securityCacheService.cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        USER_TYPE,
                        USER_ID,
                        true,
                        1L,
                        1L,
                        UserDataScopeType.SELF
                )
        );
        securityCacheService.cacheAuthorityCodes(USER_TYPE, USER_ID, List.of("admin:role", "admin:user:view"));
        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .cookie(accessTokenCookie(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1001));

        assertEquals("1|1|1|3", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
    }

    private String activateAccessToken() {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, USER_TYPE);
        Claims claims = jwtTokenUtils.parseClaims(accessToken);
        securityCacheService.markTokenActive(accessToken, claims.getExpiration());
        return accessToken;
    }

    private Cookie accessTokenCookie(String accessToken) {
        return new Cookie(CookieUtils.ACCESS_TOKEN_COOKIE_NAME, accessToken);
    }

    private void clearRedis() {
        if (stringRedisTemplate.getConnectionFactory() == null) {
            return;
        }
        try (var connection = stringRedisTemplate.getConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    private void insertCampus(Long campusId, String campusName) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_campus (id, code, name, address, phone, sort, status, remark, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                campusId, "CAMPUS-TEST-" + campusId, campusName, "杭州", "0571-00000001", 2, 1, "测试校区", NOW, NOW
        );
        jdbcTemplate.update(
                """
                INSERT INTO sys_org_node (id, parent_id, node_type, code, name, campus_id, ancestors, level, sort, status, remark, created_at, updated_at)
                VALUES (?, 0, 1, ?, ?, ?, '', 1, ?, 1, ?, ?, ?)
                """,
                campusId, "CAMPUS-TEST-" + campusId, campusName, campusId, campusId, "测试根节点", NOW, NOW
        );
    }

    private void insertInternalUser(
            Long userId,
            String username,
            String phone,
            String name,
            UserDataScopeType dataScopeType,
            Long campusId,
            Long orgNodeId
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_internal_user (id, username, password, phone, name, avatar, status, must_change_password, data_scope_type, campus_id, org_node_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
                campusId,
                orgNodeId,
                NOW,
                NOW
        );
    }

    private void bindUserOrgScope(Long userId, Long orgNodeId, boolean primary) {
        jdbcTemplate.update(
                """
                INSERT INTO sys_user_org_scope (user_id, org_node_id, is_primary, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                userId,
                orgNodeId,
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
