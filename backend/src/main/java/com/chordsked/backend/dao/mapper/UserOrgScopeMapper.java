package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("userOrgScopeMapper")
public interface UserOrgScopeMapper {
    List<UserOrgScopeEntity> listByUserId(@Param("userId") Long userId);

    List<UserOrgScopeEntity> listByOrgNodeId(@Param("orgNodeId") Long orgNodeId);

    List<Long> listUserIdsByOrgNodeIds(@Param("orgNodeIds") List<Long> orgNodeIds);

    int deleteByUserIdAndOrgNodeId(@Param("userId") Long userId, @Param("orgNodeId") Long orgNodeId);

    int deleteByUserId(@Param("userId") Long userId);

    int saveBatch(@Param("entities") List<UserOrgScopeEntity> entities);

    int countPrimaryByUserId(@Param("userId") Long userId);

    Long getPrimaryOrgNodeIdByUserId(@Param("userId") Long userId);
}
