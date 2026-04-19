package com.chordsked.backend.service.security.impl;

import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.model.enums.AccountUserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AuthorityCodeServiceImplTest {
    private AuthorityCodeServiceImpl authorityCodeService;
    private PermissionDao permissionDao;

    @BeforeEach
    void setUp() {
        authorityCodeService = new AuthorityCodeServiceImpl();
        permissionDao = mock(PermissionDao.class);
        ReflectionTestUtils.setField(authorityCodeService, "permissionDao", permissionDao);
    }

    @Test
    void shouldResolveAdminAuthoritiesByUserRoleBinding() {
        when(permissionDao.listPermissionCodesByInternalUserId(1001L)).thenReturn(List.of("admin:role"));

        List<String> result = authorityCodeService.getAuthorityCodes(AccountUserType.ADMIN, 1001L);

        assertEquals(List.of("admin:role"), result);
        verify(permissionDao).listPermissionCodesByInternalUserId(1001L);
        verifyNoMoreInteractions(permissionDao);
    }

    @Test
    void shouldResolveTeacherAuthoritiesByUserType() {
        when(permissionDao.listPermissionCodesByUserType("TEACHER")).thenReturn(List.of("teacher:role"));

        List<String> result = authorityCodeService.getAuthorityCodes(AccountUserType.TEACHER, 2001L);

        assertEquals(List.of("teacher:role"), result);
        verify(permissionDao).listPermissionCodesByUserType("TEACHER");
        verifyNoMoreInteractions(permissionDao);
    }

    @Test
    void shouldReturnEmptyListWhenUserTypeMissing() {
        assertEquals(List.of(), authorityCodeService.getAuthorityCodes(null, 1001L));
        verifyNoMoreInteractions(permissionDao);
    }
}
