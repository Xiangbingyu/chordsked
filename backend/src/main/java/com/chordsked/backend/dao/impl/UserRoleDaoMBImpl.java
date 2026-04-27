package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.dao.mapper.UserRoleMapper;
import com.chordsked.backend.model.entity.UserRoleEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userRoleDao")
public class UserRoleDaoMBImpl implements UserRoleDao {
    @Resource(name = "userRoleMapper")
    private UserRoleMapper userRoleMapper;

    @Override
    public List<UserRoleEntity> listByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRoleMapper.listByUserIds(userIds);
    }

    @Override
    public List<Long> listUserIdsByRoleId(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return List.of();
        }
        return userRoleMapper.listUserIdsByRoleId(roleId);
    }

    @Override
    public int deleteByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return userRoleMapper.deleteByUserId(userId);
    }

    @Override
    public int saveBatch(List<UserRoleEntity> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return 0;
        }
        return userRoleMapper.saveBatch(userRoles);
    }
}
