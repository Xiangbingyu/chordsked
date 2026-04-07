package com.chordsked.backend.security.account.provider;

import com.chordsked.backend.cache.security.SecurityCacheService;
import com.chordsked.backend.dao.StudentUserDao;
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

class StudentAccountProviderTest {
    private StudentAccountProvider studentAccountProvider;
    private StudentUserDao studentUserDao;
    private SecurityCacheService securityCacheService;

    @BeforeEach
    void setUp() {
        studentAccountProvider = new StudentAccountProvider();
        studentUserDao = mock(StudentUserDao.class);
        securityCacheService = mock(SecurityCacheService.class);

        ReflectionTestUtils.setField(studentAccountProvider, "studentUserDao", studentUserDao);
        ReflectionTestUtils.setField(studentAccountProvider, "securityCacheService", securityCacheService);
    }

    @Test
    void shouldLoadStudentDetailsFromSecuritySnapshotCache() {
        when(securityCacheService.getAuthorityCodes("STUDENT", 3001L)).thenReturn(List.of("student:role"));
        when(securityCacheService.getUserSnapshot("STUDENT", 3001L))
                .thenReturn(new SecurityCacheService.SecurityUserSnapshot("STUDENT", 3001L, true, 4001L));

        ChordSkedUserDetails userDetails = (ChordSkedUserDetails) studentAccountProvider.loadUserDetails(3001L);

        assertEquals(3001L, userDetails.getUserId());
        assertEquals(4001L, userDetails.getCurrentCampusId());
        assertTrue(userDetails.isEnabled());
        verify(studentUserDao, never()).getById(3001L);
    }
}
