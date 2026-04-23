package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.datascope.annotation.DataScope;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("internalUserMapper")
public interface InternalUserMapper {
    InternalUserEntity getById(@Param("userId") Long userId);

    InternalUserEntity getByUsername(@Param("username") String username);

    InternalUserEntity getByPhone(@Param("phone") String phone);

    @DataScope(tableAlias = "u", scopeField = "id")
    List<InternalUserQueryResultVO> listByQuery(@Param("request") InternalUserQueryRequest request);

    @DataScope(tableAlias = "u", scopeField = "id")
    Long countByQuery(@Param("request") InternalUserQueryRequest request);

    int save(@Param("user") InternalUserEntity user);

    int updateById(@Param("user") InternalUserEntity user);

    @DataScope(tableAlias = "u", scopeField = "id")
    Long countAccessibleById(@Param("userId") Long userId);

    @DataScope(tableAlias = "u", scopeField = "id")
    InternalUserDetailResultVO getAccessibleDetailById(@Param("userId") Long userId);

    int updateStatus(
            @Param("userId") Long userId,
            @Param("status") Integer status,
            @Param("updatedAt") Long updatedAt
    );

    int updatePassword(
            @Param("userId") Long userId,
            @Param("password") String password,
            @Param("mustChangePassword") Integer mustChangePassword,
            @Param("updatedAt") Long updatedAt
    );

    Long countRoleCodeByUserId(
            @Param("userId") Long userId,
            @Param("roleCode") String roleCode
    );

    Long countEnabledUsersByRoleCode(@Param("roleCode") String roleCode);
}
