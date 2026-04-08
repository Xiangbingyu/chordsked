package com.chordsked.backend.service.verification.userstatus.provider;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.InternalUserStatus;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 管理员账号状态校验 provider。
 */
@Component("internalUserStatusVerificationProvider")
public class InternalUserStatusVerificationProvider implements UserStatusVerificationProvider {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Override
    public AccountUserType getUserType() {
        return AccountUserType.ADMIN;
    }

    @Override
    public void verify(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        InternalUserEntity internalUser = internalUserDao.getById(userId);
        if (internalUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        if (!InternalUserStatus.ENABLED.equals(internalUser.getStatusEnum())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号已被禁用，请联系管理员");
        }
    }
}
