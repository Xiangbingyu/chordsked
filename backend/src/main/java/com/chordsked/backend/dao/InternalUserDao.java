package com.chordsked.backend.dao;

import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;

import java.util.List;

public interface InternalUserDao {
    InternalUserEntity getById(Long userId);

    InternalUserEntity getByUsername(String username);

    InternalUserEntity getByPhone(String phone);

    List<InternalUserQueryResultVO> listByQuery(InternalUserQueryRequest request);

    Long countByQuery(InternalUserQueryRequest request);

    int save(InternalUserEntity user);
}
