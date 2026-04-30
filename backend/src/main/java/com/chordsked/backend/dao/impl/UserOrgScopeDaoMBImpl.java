package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.dao.mapper.UserOrgScopeMapper;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("userOrgScopeDao")
public class UserOrgScopeDaoMBImpl implements UserOrgScopeDao {
    @Resource(name = "userOrgScopeMapper")
    private UserOrgScopeMapper userOrgScopeMapper;

    @Override
    public List<UserOrgScopeEntity> listByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return userOrgScopeMapper.listByUserId(userId);
    }

    @Override
    public List<UserOrgScopeEntity> listByOrgNodeId(Long orgNodeId) {
        if (orgNodeId == null || orgNodeId <= 0) {
            return List.of();
        }
        return userOrgScopeMapper.listByOrgNodeId(orgNodeId);
    }

    @Override
    public List<Long> listUserIdsByOrgNodeIds(List<Long> orgNodeIds) {
        if (orgNodeIds == null || orgNodeIds.isEmpty()) {
            return List.of();
        }
        return userOrgScopeMapper.listUserIdsByOrgNodeIds(orgNodeIds);
    }

    @Override
    public int deleteByUserIdAndOrgNodeId(Long userId, Long orgNodeId) {
        if (userId == null || userId <= 0 || orgNodeId == null || orgNodeId <= 0) {
            return 0;
        }
        return userOrgScopeMapper.deleteByUserIdAndOrgNodeId(userId, orgNodeId);
    }

    @Override
    public int deleteByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return userOrgScopeMapper.deleteByUserId(userId);
    }

    @Override
    public int saveBatch(List<UserOrgScopeEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return 0;
        }
        return userOrgScopeMapper.saveBatch(entities);
    }

    @Override
    public int countPrimaryByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return userOrgScopeMapper.countPrimaryByUserId(userId);
    }

    @Override
    public Long getPrimaryOrgNodeIdByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userOrgScopeMapper.getPrimaryOrgNodeIdByUserId(userId);
    }
}
