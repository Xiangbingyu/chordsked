package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.mapper.InternalUserMapper;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;
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

    @Override
    public int updateById(InternalUserEntity user) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            return 0;
        }
        return internalUserMapper.updateById(user);
    }

    @Override
    public boolean existsAccessibleById(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        Long count = internalUserMapper.countAccessibleById(userId);
        return count != null && count > 0;
    }

    @Override
    public InternalUserDetailResultVO getAccessibleDetailById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return internalUserMapper.getAccessibleDetailById(userId);
    }

    @Override
    public int updateStatus(Long userId, Integer status, Long updatedAt) {
        if (userId == null || userId <= 0 || status == null || updatedAt == null || updatedAt <= 0) {
            return 0;
        }
        return internalUserMapper.updateStatus(userId, status, updatedAt);
    }

    @Override
    public int updatePassword(Long userId, String password, Integer mustChangePassword, Long updatedAt) {
        if (userId == null
                || userId <= 0
                || password == null
                || password.isBlank()
                || mustChangePassword == null
                || updatedAt == null
                || updatedAt <= 0) {
            return 0;
        }
        return internalUserMapper.updatePassword(userId, password, mustChangePassword, updatedAt);
    }

    @Override
    public boolean hasRoleCode(Long userId, String roleCode) {
        if (userId == null || userId <= 0 || roleCode == null || roleCode.isBlank()) {
            return false;
        }
        Long count = internalUserMapper.countRoleCodeByUserId(userId, roleCode.trim());
        return count != null && count > 0;
    }

    @Override
    public Long countEnabledUsersByRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return 0L;
        }
        Long count = internalUserMapper.countEnabledUsersByRoleCode(roleCode.trim());
        return count == null ? 0L : count;
    }
}
