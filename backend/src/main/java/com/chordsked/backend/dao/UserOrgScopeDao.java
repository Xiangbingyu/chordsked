package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.UserOrgScopeEntity;

import java.util.List;

public interface UserOrgScopeDao {
    List<UserOrgScopeEntity> listByUserId(Long userId);

    List<UserOrgScopeEntity> listByOrgNodeId(Long orgNodeId);

    List<Long> listUserIdsByOrgNodeIds(List<Long> orgNodeIds);

    int deleteByUserIdAndOrgNodeId(Long userId, Long orgNodeId);

    int deleteByUserId(Long userId);

    int saveBatch(List<UserOrgScopeEntity> entities);

    int countPrimaryByUserId(Long userId);

    Long getPrimaryOrgNodeIdByUserId(Long userId);
}
