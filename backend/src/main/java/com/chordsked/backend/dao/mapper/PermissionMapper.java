package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.PermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("permissionMapper")
public interface PermissionMapper {
    PermissionEntity getById(@Param("id") Long id);

    List<PermissionEntity> listByIds(@Param("ids") List<Long> ids);

    List<PermissionEntity> listByUserType(@Param("userType") String userType);

    List<PermissionEntity> listByRoleId(@Param("roleId") Long roleId);

    List<PermissionEntity> listByInternalUserId(@Param("userId") Long userId);

    List<String> listPermissionCodesByInternalUserId(@Param("userId") Long userId);

    List<String> listPermissionCodesByUserType(@Param("userType") String userType);
}
