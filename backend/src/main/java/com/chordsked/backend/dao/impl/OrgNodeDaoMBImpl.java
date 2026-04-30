package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.mapper.OrgNodeMapper;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("orgNodeDao")
public class OrgNodeDaoMBImpl implements OrgNodeDao {
    @Resource(name = "orgNodeMapper")
    private OrgNodeMapper orgNodeMapper;

    @Override
    public OrgNodeEntity getById(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return null;
        }
        return orgNodeMapper.getById(nodeId);
    }

    @Override
    public List<OrgNodeEntity> listAllEnabled() {
        return orgNodeMapper.listAllEnabled();
    }

    @Override
    public List<OrgNodeEntity> listChildrenByParentId(Long parentId) {
        if (parentId == null || parentId < 0) {
            return List.of();
        }
        return orgNodeMapper.listChildrenByParentId(parentId);
    }

    @Override
    public List<OrgNodeEntity> listDescendantsByNodeId(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return List.of();
        }
        return orgNodeMapper.listDescendantsByNodeId(nodeId);
    }

    @Override
    public int countChildrenByParentId(Long parentId) {
        if (parentId == null || parentId < 0) {
            return 0;
        }
        return orgNodeMapper.countChildrenByParentId(parentId);
    }

    @Override
    public int countUserBindingsByNodeId(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return 0;
        }
        return orgNodeMapper.countUserBindingsByNodeId(nodeId);
    }

    @Override
    public int save(OrgNodeEntity entity) {
        if (entity == null) {
            return 0;
        }
        return orgNodeMapper.save(entity);
    }

    @Override
    public int updateById(OrgNodeEntity entity) {
        if (entity == null || entity.getId() == null || entity.getId() <= 0) {
            return 0;
        }
        return orgNodeMapper.updateById(entity);
    }

    @Override
    public int deleteById(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return 0;
        }
        return orgNodeMapper.deleteById(nodeId);
    }

    @Override
    public boolean existsByParentIdAndCode(Long parentId, String code, Long excludeId) {
        if (parentId == null || parentId < 0 || code == null || code.isBlank()) {
            return false;
        }
        return orgNodeMapper.existsByParentIdAndCode(parentId, code.trim(), excludeId) > 0;
    }

    @Override
    public boolean existsByParentIdAndName(Long parentId, String name, Long excludeId) {
        if (parentId == null || parentId < 0 || name == null || name.isBlank()) {
            return false;
        }
        return orgNodeMapper.existsByParentIdAndName(parentId, name.trim(), excludeId) > 0;
    }
}
