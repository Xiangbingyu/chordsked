package com.chordsked.backend.model.permission;

import com.chordsked.backend.model.enums.AccountUserType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionCodeResolverTest {

    @Test
    void shouldResolvePermissionCodesByAccountUserType() {
        assertEquals(List.of("admin:role"), PermissionCodeResolver.resolvePermissionCodes(AccountUserType.ADMIN));
        assertEquals(List.of("teacher:role"), PermissionCodeResolver.resolvePermissionCodes(AccountUserType.TEACHER));
        assertEquals(List.of("student:role"), PermissionCodeResolver.resolvePermissionCodes(AccountUserType.STUDENT));
    }

    @Test
    void shouldReturnEmptyListWhenUserTypeInvalid() {
        assertEquals(List.of(), PermissionCodeResolver.resolvePermissionCodes((String) null));
        assertEquals(List.of(), PermissionCodeResolver.resolvePermissionCodes(" "));
        assertEquals(List.of(), PermissionCodeResolver.resolvePermissionCodes("guest"));
    }
}
