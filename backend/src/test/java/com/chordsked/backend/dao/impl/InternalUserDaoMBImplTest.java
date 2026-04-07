package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.mapper.InternalUserMapper;
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
    private InternalUserMapper internalUserMapper;

    @BeforeEach
    void setUp() {
        internalUserDao = new InternalUserDaoMBImpl();
        internalUserMapper = mock(InternalUserMapper.class);
        ReflectionTestUtils.setField(internalUserDao, "internalUserMapper", internalUserMapper);
    }

    @Test
    void shouldReturnNullWhenUserIdInvalid() {
        assertNull(internalUserDao.getById(null));
        assertNull(internalUserDao.getById(0L));

        verifyNoInteractions(internalUserMapper);
    }

    @Test
    void shouldTrimUsernameBeforeQuery() {
        InternalUserEntity internalUser = new InternalUserEntity();
        when(internalUserMapper.getByUsername("admin")).thenReturn(internalUser);

        internalUserDao.getByUsername("  admin  ");

        verify(internalUserMapper).getByUsername("admin");
    }

    @Test
    void shouldReturnNullWhenUsernameInvalid() {
        assertNull(internalUserDao.getByUsername(null));
        assertNull(internalUserDao.getByUsername("   "));

        verifyNoInteractions(internalUserMapper);
    }
}
