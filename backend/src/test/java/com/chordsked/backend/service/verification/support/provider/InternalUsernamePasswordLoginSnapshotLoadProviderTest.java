package com.chordsked.backend.service.verification.support.provider;

import com.chordsked.backend.cache.auth.provider.AuthLoginSnapshotCacheProvider;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.model.auth.AuthLoginMethod;
import com.chordsked.backend.model.auth.UsernamePasswordLoginSnapshot;
import com.chordsked.backend.model.dto.auth.AuthLoginRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalUsernamePasswordLoginSnapshotLoadProviderTest {
    private InternalUsernamePasswordLoginSnapshotLoadProvider provider;
    private AuthLoginSnapshotCacheProvider snapshotCacheProvider;
    private InternalUserDao internalUserDao;

    @BeforeEach
    void setUp() {
        provider = new InternalUsernamePasswordLoginSnapshotLoadProvider();
        snapshotCacheProvider = mock(AuthLoginSnapshotCacheProvider.class);
        internalUserDao = mock(InternalUserDao.class);

        ReflectionTestUtils.setField(provider, "usernamePasswordLoginSnapshotCacheProvider", snapshotCacheProvider);
        ReflectionTestUtils.setField(provider, "internalUserDao", internalUserDao);
    }

    @Test
    void shouldLoadSnapshotFromCache() {
        UsernamePasswordLoginSnapshot cachedSnapshot = new UsernamePasswordLoginSnapshot();
        cachedSnapshot.setUserId(1001L);
        cachedSnapshot.setUserType(AccountUserType.ADMIN);
        cachedSnapshot.setLoginMethod(AuthLoginMethod.USERNAME_PASSWORD);
        cachedSnapshot.setPrincipal("admin");
        cachedSnapshot.setDataScopeType(UserDataScopeType.ALL);
        when(snapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, "admin")).thenReturn(cachedSnapshot);

        UsernamePasswordLoginSnapshot snapshot = (UsernamePasswordLoginSnapshot) provider.load(
                new AuthLoginRequest(),
                AccountUserType.ADMIN,
                "admin"
        );

        assertEquals(UserDataScopeType.ALL, snapshot.getDataScopeType());
        verify(internalUserDao, never()).getByUsername(any());
    }

    @Test
    void shouldLoadSnapshotFromDatabaseAndCarryDataScopeType() {
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(1001L);
        internalUser.setUsername("admin");
        internalUser.setName("系统管理员");
        internalUser.setStatus(1);
        internalUser.setMustChangePassword(0);
        internalUser.setPassword("encodedPassword");
        internalUser.setPhone("13800138000");
        internalUser.setDataScopeType(UserDataScopeType.ASSIGNED.getCode());
        when(snapshotCacheProvider.getLoginSnapshot(AccountUserType.ADMIN, "admin")).thenReturn(null);
        when(internalUserDao.getByUsername("admin")).thenReturn(internalUser);

        UsernamePasswordLoginSnapshot snapshot = (UsernamePasswordLoginSnapshot) provider.load(
                new AuthLoginRequest(),
                AccountUserType.ADMIN,
                "admin"
        );

        assertNotNull(snapshot);
        assertEquals(1001L, snapshot.getUserId());
        assertEquals("admin", snapshot.getPrincipal());
        assertEquals(UserDataScopeType.ASSIGNED, snapshot.getDataScopeType());
        verify(snapshotCacheProvider).cacheLoginSnapshot(eq(snapshot));
    }
}
