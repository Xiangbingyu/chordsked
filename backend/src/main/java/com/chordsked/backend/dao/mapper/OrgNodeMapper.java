package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.OrgNodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("orgNodeMapper")
public interface OrgNodeMapper {
    OrgNodeEntity getById(@Param("nodeId") Long nodeId);

    List<OrgNodeEntity> listAllEnabled();

    List<OrgNodeEntity> listChildrenByParentId(@Param("parentId") Long parentId);

    List<OrgNodeEntity> listDescendantsByNodeId(@Param("nodeId") Long nodeId);

    int countChildrenByParentId(@Param("parentId") Long parentId);

    int countUserBindingsByNodeId(@Param("nodeId") Long nodeId);

    int save(@Param("entity") OrgNodeEntity entity);

    int updateById(@Param("entity") OrgNodeEntity entity);

    int deleteById(@Param("nodeId") Long nodeId);

    int existsByParentIdAndCode(@Param("parentId") Long parentId, @Param("code") String code, @Param("excludeId") Long excludeId);

    int existsByParentIdAndName(@Param("parentId") Long parentId, @Param("name") String name, @Param("excludeId") Long excludeId);
}
