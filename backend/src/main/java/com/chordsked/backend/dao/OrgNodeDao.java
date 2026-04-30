package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.OrgNodeEntity;

import java.util.List;

public interface OrgNodeDao {
    OrgNodeEntity getById(Long nodeId);

    List<OrgNodeEntity> listAllEnabled();

    List<OrgNodeEntity> listChildrenByParentId(Long parentId);

    List<OrgNodeEntity> listDescendantsByNodeId(Long nodeId);

    int countChildrenByParentId(Long parentId);

    int countUserBindingsByNodeId(Long nodeId);

    int save(OrgNodeEntity entity);

    int updateById(OrgNodeEntity entity);

    int deleteById(Long nodeId);

    boolean existsByParentIdAndCode(Long parentId, String code, Long excludeId);

    boolean existsByParentIdAndName(Long parentId, String name, Long excludeId);
}
