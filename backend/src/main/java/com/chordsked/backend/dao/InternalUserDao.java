package com.chordsked.backend.dao;

import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;

import java.util.List;

public interface InternalUserDao {
    InternalUserEntity getById(Long userId);

    InternalUserEntity getByUsername(String username);

    InternalUserEntity getByPhone(String phone);

    List<InternalUserQueryResultVO> listByQuery(InternalUserQueryRequest request);

    Long countByQuery(InternalUserQueryRequest request);

    int save(InternalUserEntity user);

    int updateById(InternalUserEntity user);

    boolean existsAccessibleById(Long userId);

    InternalUserDetailResultVO getAccessibleDetailById(Long userId);

    int updateStatus(Long userId, Integer status, Long updatedAt);

    int updatePassword(Long userId, String password, Integer mustChangePassword, Long updatedAt);

    boolean hasRoleCode(Long userId, String roleCode);

    Long countEnabledUsersByRoleCode(String roleCode);
}
