package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.mapper.RoleMapper;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("roleDao")
public class RoleDaoMBImpl implements RoleDao {
    @Resource(name = "roleMapper")
    private RoleMapper roleMapper;

    @Override
    public RoleEntity getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return roleMapper.getById(id);
    }

    @Override
    public RoleEntity getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return roleMapper.getByCode(code.trim());
    }

    @Override
    public RoleDetailQueryResultVO getDetailById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return roleMapper.getDetailById(id);
    }

    @Override
    public int save(RoleEntity role) {
        if (role == null) {
            return 0;
        }
        return roleMapper.save(role);
    }

    @Override
    public Long countUserBinding(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return 0L;
        }
        Long total = roleMapper.countUserBinding(roleId);
        return total == null ? 0L : total;
    }

    @Override
    public int updateById(RoleEntity role) {
        if (role == null || role.getId() == null || role.getId() <= 0) {
            return 0;
        }
        return roleMapper.updateById(role);
    }

    @Override
    public int deleteById(Long roleId) {
        if (roleId == null || roleId <= 0) {
            return 0;
        }
        return roleMapper.deleteById(roleId);
    }

    @Override
    public List<RoleQueryResultVO> listByQuery(RoleQueryRequest request) {
        if (!isValidRoleQueryRequest(request)) {
            return List.of();
        }
        return roleMapper.listByQuery(request);
    }

    @Override
    public Long countByQuery(RoleQueryRequest request) {
        if (!isValidRoleQueryRequest(request)) {
            return 0L;
        }
        Long total = roleMapper.countByQuery(request);
        return total == null ? 0L : total;
    }

    private boolean isValidRoleQueryRequest(RoleQueryRequest request) {
        if (request == null) {
            return false;
        }
        Integer page = request.getPage();
        if (page == null || page < 1) {
            return false;
        }
        Integer pageSize = request.getPageSize();
        if (pageSize == null || pageSize < 1) {
            return false;
        }
        Integer status = request.getStatus();
        return status == null || RoleStatus.fromCode(status) != null;
    }
}
