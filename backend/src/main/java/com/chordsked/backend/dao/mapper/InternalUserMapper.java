package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.InternalUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("internalUserMapper")
public interface InternalUserMapper {
    InternalUserEntity getById(@Param("userId") Long userId);

    InternalUserEntity getByUsername(@Param("username") String username);
}
