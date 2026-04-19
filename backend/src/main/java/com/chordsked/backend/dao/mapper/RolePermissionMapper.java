package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.RolePermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("rolePermissionMapper")
public interface RolePermissionMapper {
    RolePermissionEntity getById(@Param("id") Long id);

    List<RolePermissionEntity> listByRoleId(@Param("roleId") Long roleId);

    List<RolePermissionEntity> listByPermissionId(@Param("permissionId") Long permissionId);
}
