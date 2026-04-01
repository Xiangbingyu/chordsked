package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.InternalUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Mapper
@Repository("internalLoginMapper")
public interface InternalLoginMapper {
    InternalUserEntity selectInternalUserByUsername(@Param("username") String username);

    int increaseInternalUserLoginFail(
            @Param("userId") Long userId,
            @Param("lockedUntil") LocalDateTime lockedUntil,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    int resetInternalUserLoginFail(
            @Param("userId") Long userId,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}
