package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.UserRoleEntity;

import java.util.List;

public interface UserRoleDao {
    List<UserRoleEntity> listByUserIds(List<Long> userIds);

    List<Long> listUserIdsByRoleId(Long roleId);

    int deleteByUserId(Long userId);

    int saveBatch(List<UserRoleEntity> userRoles);
}
