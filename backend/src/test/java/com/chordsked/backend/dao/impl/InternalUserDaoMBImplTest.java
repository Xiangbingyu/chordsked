package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.mapper.InternalLoginMapper;
import com.chordsked.backend.model.entity.InternalUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InternalUserDaoMBImplTest {
    private InternalUserDaoMBImpl internalUserDao;
    private InternalLoginMapper internalLoginMapper;

    @BeforeEach
    void setUp() {
        internalUserDao = new InternalUserDaoMBImpl();
        internalLoginMapper = mock(InternalLoginMapper.class);
        ReflectionTestUtils.setField(internalUserDao, "internalLoginMapper", internalLoginMapper);
    }

    @Test
    void shouldReturnNullWhenUserIdInvalid() {
        assertNull(internalUserDao.getById(null));
        assertNull(internalUserDao.getById(0L));

        verifyNoInteractions(internalLoginMapper);
    }

    @Test
    void shouldTrimUsernameBeforeQuery() {
        InternalUserEntity internalUser = new InternalUserEntity();
        when(internalLoginMapper.getByUsername("admin")).thenReturn(internalUser);

        internalUserDao.getByUsername("  admin  ");

        verify(internalLoginMapper).getByUsername("admin");
    }

    @Test
    void shouldReturnNullWhenUsernameInvalid() {
        assertNull(internalUserDao.getByUsername(null));
        assertNull(internalUserDao.getByUsername("   "));

        verifyNoInteractions(internalLoginMapper);
    }
}
