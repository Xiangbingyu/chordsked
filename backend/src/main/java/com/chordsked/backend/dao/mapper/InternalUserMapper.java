package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.datascope.annotation.DataScope;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
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
}
