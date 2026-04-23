package com.chordsked.backend.controller;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import io.jsonwebtoken.Claims;
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
        insertCampus(2L, "第二校区");
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(1003L, "self_user", "13800000002", "本人账号", UserDataScopeType.SELF_ONLY);
        bindUserCampus(1002L, 1L, true);
        bindUserCampus(1003L, 2L, true);
        bindUserRole(1002L, 2L);
        bindUserRole(1003L, 2L);

        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002))
                .andExpect(jsonPath("$.data.items[2].id").value(1003));

        SecurityCacheService.SecurityUserSnapshot snapshot = securityCacheService.getUserSnapshot(USER_TYPE, USER_ID);
        assertNotNull(snapshot);
        assertEquals(UserDataScopeType.ALL_COMPANY, snapshot.dataScopeType());
        assertEquals(1L, snapshot.currentCampusId());
        assertTrue(securityCacheService.getAuthorityCodes(USER_TYPE, USER_ID).contains("admin:user:view"));
        assertEquals("1|1|1", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
        assertTrue(stringRedisTemplate.opsForValue().get(AUTHORITY_KEY).contains("admin:user:view"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUseRedisSnapshotForCampusScopeAndTriggerDataScopeFilter() throws Exception {
        insertCampus(2L, "第二校区");
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        insertInternalUser(1003L, "other_campus", "13800000002", "跨校区账号", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(1002L, 1L, true);
        bindUserCampus(1003L, 2L, true);
        bindUserRole(1002L, 2L);
        bindUserRole(1003L, 2L);
        securityCacheService.cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        USER_TYPE,
                        USER_ID,
                        true,
                        1L,
                        UserDataScopeType.SPECIFIED_CAMPUS
                )
        );
        securityCacheService.cacheAuthorityCodes(USER_TYPE, USER_ID, List.of("admin:role", "admin:user:view"));
        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(1001))
                .andExpect(jsonPath("$.data.items[1].id").value(1002));

        assertEquals("1|1|4", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUseRedisSnapshotForSelfScopeAndTriggerDataScopeFilter() throws Exception {
        insertInternalUser(1002L, "campus_admin", "13800000001", "校区管理员", UserDataScopeType.SPECIFIED_CAMPUS);
        bindUserCampus(1002L, 1L, true);
        bindUserRole(1002L, 2L);
        securityCacheService.cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        USER_TYPE,
                        USER_ID,
                        true,
                        1L,
                        UserDataScopeType.SELF_ONLY
                )
        );
        securityCacheService.cacheAuthorityCodes(USER_TYPE, USER_ID, List.of("admin:role", "admin:user:view"));
        String accessToken = activateAccessToken();

        mockMvc.perform(get("/admin/api/v1/internal-users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(1001));

        assertEquals("1|1|3", stringRedisTemplate.opsForValue().get(USER_SNAPSHOT_KEY));
    }

    private String activateAccessToken() {
        String accessToken = jwtTokenUtils.generateAccessToken(USER_ID, USER_TYPE);
        Claims claims = jwtTokenUtils.parseClaims(accessToken);
        securityCacheService.markTokenActive(accessToken, claims.getExpiration());
        return accessToken;
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
