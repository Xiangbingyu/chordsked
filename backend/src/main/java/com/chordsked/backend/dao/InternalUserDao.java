package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.InternalUserEntity;

public interface InternalUserDao {
    InternalUserEntity getById(Long userId);

    InternalUserEntity getByUsername(String username);
}
