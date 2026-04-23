package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("userRoleMapper")
public interface UserRoleMapper {
    List<UserRoleEntity> listByUserIds(@Param("userIds") List<Long> userIds);

    int deleteByUserId(@Param("userId") Long userId);

    int saveBatch(@Param("userRoles") List<UserRoleEntity> userRoles);
}
