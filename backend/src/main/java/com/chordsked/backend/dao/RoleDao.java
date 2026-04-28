package com.chordsked.backend.dao;

import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;

import java.util.List;

public interface RoleDao {
    RoleEntity getById(Long id);

    RoleEntity getByCode(String code);

    RoleDetailQueryResultVO getDetailById(Long id);

    int save(RoleEntity role);

    Long countUserBinding(Long roleId);

    int updateById(RoleEntity role);

    int deleteById(Long roleId);

    List<RoleQueryResultVO> listByQuery(RoleQueryRequest request, List<String> excludedRoleCodes);

    Long countByQuery(RoleQueryRequest request, List<String> excludedRoleCodes);
}
