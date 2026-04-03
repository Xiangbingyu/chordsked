package com.chordsked.backend.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountUserTypeTest {
    @Test
    void shouldResolveUserTypeFromValueIgnoringCase() {
        assertEquals(AccountUserType.ADMIN, AccountUserType.fromValue("admin"));
        assertEquals(AccountUserType.TEACHER, AccountUserType.fromValue(" TEACHER "));
        assertEquals(AccountUserType.STUDENT, AccountUserType.fromValue("student"));
    }

    @Test
    void shouldRejectBlankUserTypeValue() {
        assertThrows(IllegalArgumentException.class, () -> AccountUserType.fromValue(" "));
    }

    @Test
    void shouldMapLoginLogUserTypeFromAccountUserType() {
        assertEquals(LoginLogUserType.ADMIN, LoginLogUserType.fromAccountUserType(AccountUserType.ADMIN));
        assertEquals(LoginLogUserType.TEACHER, LoginLogUserType.fromAccountUserType(AccountUserType.TEACHER));
        assertEquals(LoginLogUserType.STUDENT, LoginLogUserType.fromAccountUserType(AccountUserType.STUDENT));
    }
}
