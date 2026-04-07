package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.security.account.model.ChordSkedUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalAccountProviderTest {
    private InternalAccountProvider internalAccountProvider;
    private InternalUserDao internalUserDao;
    private UserCampusDao userCampusDao;
    private SecurityCacheService securityCacheService;

    @BeforeEach
    void setUp() {
        internalAccountProvider = new InternalAccountProvider();
        internalUserDao = mock(InternalUserDao.class);
        userCampusDao = mock(UserCampusDao.class);
        securityCacheService = mock(SecurityCacheService.class);

        ReflectionTestUtils.setField(internalAccountProvider, "internalUserDao", internalUserDao);
        ReflectionTestUtils.setField(internalAccountProvider, "userCampusDao", userCampusDao);
        ReflectionTestUtils.setField(internalAccountProvider, "securityCacheService", securityCacheService);
    }

    @Test
    void shouldLoadUserDetailsFromSecuritySnapshotCache() {
        when(securityCacheService.getAuthorityCodes("ADMIN", 1001L)).thenReturn(List.of("admin:role"));
        when(securityCacheService.getUserSnapshot("ADMIN", 1001L))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot("ADMIN", 1001L, true, 2001L));

        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) internalAccountProvider.loadUserDetails(1001L);

        assertEquals(1001L, userDetails.getUserId());
        assertEquals(2001L, userDetails.getCurrentCampusId());
        assertTrue(userDetails.isEnabled());
        verify(internalUserDao, never()).getById(1001L);
        verify(userCampusDao, never()).getPrimaryCampusIdByUserId(1001L);
    }

    @Test
    void shouldLoadUserDetailsFromDatabaseWhenSecuritySnapshotCacheMiss() {
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(1001L);
        internalUser.setStatus(1);

        when(securityCacheService.getAuthorityCodes("ADMIN", 1001L)).thenReturn(List.of("admin:role"));
        when(securityCacheService.getUserSnapshot("ADMIN", 1001L)).thenReturn(null);
        when(internalUserDao.getById(1001L)).thenReturn(internalUser);
        when(userCampusDao.getPrimaryCampusIdByUserId(1001L)).thenReturn(2001L);

        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) internalAccountProvider.loadUserDetails(1001L);

        assertEquals(1001L, userDetails.getUserId());
        assertEquals(2001L, userDetails.getCurrentCampusId());
        assertTrue(userDetails.isEnabled());
        verify(securityCacheService).cacheUserSnapshot(new SecurityCacheService.SecurityUserSnapshot("ADMIN", 1001L, true, 2001L));
    }
}
