package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.mapper.InternalUserMapper;
import com.chordsked.backend.model.entity.InternalUserEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("internalUserDao")
public class InternalUserDaoMBImpl implements InternalUserDao {
    @Resource(name = "internalUserMapper")
    private InternalUserMapper internalUserMapper;

    @Override
    public InternalUserEntity getById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return internalUserMapper.getById(userId);
    }

    @Override
    public InternalUserEntity getByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return internalUserMapper.getByUsername(username.trim());
    }
}
