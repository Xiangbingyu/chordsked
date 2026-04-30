package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.org.OrgDataScopeResolveService;
import com.chordsked.backend.service.security.AuthorityCodeService;
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
    private SecurityCacheService securityCacheService;
    private AuthorityCodeService authorityCodeService;
    private OrgDataScopeResolveService orgDataScopeResolveService;

    @BeforeEach
    void setUp() {
        internalAccountProvider = new InternalAccountProvider();
        internalUserDao = mock(InternalUserDao.class);
        securityCacheService = mock(SecurityCacheService.class);
        authorityCodeService = mock(AuthorityCodeService.class);
        orgDataScopeResolveService = mock(OrgDataScopeResolveService.class);

        ReflectionTestUtils.setField(internalAccountProvider, "internalUserDao", internalUserDao);
        ReflectionTestUtils.setField(internalAccountProvider, "securityCacheService", securityCacheService);
        ReflectionTestUtils.setField(internalAccountProvider, "authorityCodeService", authorityCodeService);
        ReflectionTestUtils.setField(internalAccountProvider, "orgDataScopeResolveService", orgDataScopeResolveService);
    }

    @Test
    void shouldLoadUserDetailsFromSecuritySnapshotCache() {
        when(securityCacheService.getAuthorityCodes("ADMIN", 1001L)).thenReturn(List.of("admin:role"));
        when(securityCacheService.getUserSnapshot("ADMIN", 1001L))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        1001L,
                        true,
                        2001L,
                        11L,
                        UserDataScopeType.ASSIGNED
                ));

        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) internalAccountProvider.loadUserDetails(1001L);

        assertEquals(1001L, userDetails.getUserId());
        assertEquals(2001L, userDetails.getCurrentCampusId());
        assertEquals(11L, userDetails.getPrimaryOrgNodeId());
        assertEquals(UserDataScopeType.ASSIGNED, userDetails.getDataScopeTypeEnum());
        assertTrue(userDetails.isEnabled());
        verify(internalUserDao, never()).getById(1001L);
    }

    @Test
    void shouldLoadUserDetailsFromDatabaseWhenSecuritySnapshotCacheMiss() {
        InternalUserEntity internalUser = new InternalUserEntity();
        internalUser.setId(1001L);
        internalUser.setStatus(1);
        internalUser.setDataScopeType(UserDataScopeType.SELF.getCode());
        internalUser.setCampusId(2001L);
        internalUser.setOrgNodeId(11L);

        when(securityCacheService.getAuthorityCodes("ADMIN", 1001L)).thenReturn(List.of("admin:role"));
        when(securityCacheService.getUserSnapshot("ADMIN", 1001L)).thenReturn(null);
        when(internalUserDao.getById(1001L)).thenReturn(internalUser);
        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) internalAccountProvider.loadUserDetails(1001L);

        assertEquals(1001L, userDetails.getUserId());
        assertEquals(2001L, userDetails.getCurrentCampusId());
        assertEquals(11L, userDetails.getPrimaryOrgNodeId());
        assertEquals(UserDataScopeType.SELF, userDetails.getDataScopeTypeEnum());
        assertTrue(userDetails.isEnabled());
        verify(securityCacheService).cacheUserSnapshot(
                new SecurityCacheService.SecurityUserSnapshot(
                        "ADMIN",
                        1001L,
                        true,
                        2001L,
                        11L,
                        UserDataScopeType.SELF
                )
        );
    }
}
