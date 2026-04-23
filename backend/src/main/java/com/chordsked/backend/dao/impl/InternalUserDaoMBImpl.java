package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.mapper.InternalUserMapper;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("internalUserDao")
public class InternalUserDaoMBImpl implements InternalUserDao {
    @Resource(name = "internalUserMapper")
    private InternalUserMapper internalUserMapper;

    @Override
    public InternalUserEntity getById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return internalUserMapper.getById(userId);
    }

    @Override
    public InternalUserEntity getByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return internalUserMapper.getByUsername(username.trim());
    }

    @Override
    public InternalUserEntity getByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return internalUserMapper.getByPhone(phone.trim());
    }

    @Override
    public List<InternalUserQueryResultVO> listByQuery(InternalUserQueryRequest request) {
        if (request == null) {
            return List.of();
        }
        return internalUserMapper.listByQuery(request);
    }

    @Override
    public Long countByQuery(InternalUserQueryRequest request) {
        if (request == null) {
            return 0L;
        }
        return internalUserMapper.countByQuery(request);
    }

    @Override
    public int save(InternalUserEntity user) {
        if (user == null) {
            return 0;
        }
        return internalUserMapper.save(user);
    }
}
