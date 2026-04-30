package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.model.enums.UserDataScopeType;
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

class TeacherAccountProviderTest {
    private TeacherAccountProvider teacherAccountProvider;
    private TeacherUserDao teacherUserDao;
    private SecurityCacheService securityCacheService;
    private AuthorityCodeService authorityCodeService;

    @BeforeEach
    void setUp() {
        teacherAccountProvider = new TeacherAccountProvider();
        teacherUserDao = mock(TeacherUserDao.class);
        securityCacheService = mock(SecurityCacheService.class);
        authorityCodeService = mock(AuthorityCodeService.class);

        ReflectionTestUtils.setField(teacherAccountProvider, "teacherUserDao", teacherUserDao);
        ReflectionTestUtils.setField(teacherAccountProvider, "securityCacheService", securityCacheService);
        ReflectionTestUtils.setField(teacherAccountProvider, "authorityCodeService", authorityCodeService);
    }

    @Test
    void shouldLoadTeacherDetailsFromSecuritySnapshotCache() {
        when(securityCacheService.getAuthorityCodes("TEACHER", 2001L)).thenReturn(List.of("teacher:role"));
        when(securityCacheService.getUserSnapshot("TEACHER", 2001L))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot("TEACHER", 2001L, true, 3001L, null, null));

        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) teacherAccountProvider.loadUserDetails(2001L);

        assertEquals(2001L, userDetails.getUserId());
        assertEquals(3001L, userDetails.getCurrentCampusId());
        assertEquals(UserDataScopeType.ASSIGNED, userDetails.getDataScopeTypeEnum());
        assertTrue(userDetails.isEnabled());
        verify(teacherUserDao, never()).getById(2001L);
    }
}
