package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.config.properties.InternalUserProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserResetPasswordRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.internaluser.InternalUserResetPasswordService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("internalUserResetPasswordService")
public class InternalUserResetPasswordServiceImpl implements InternalUserResetPasswordService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "internalUserProperties")
    private InternalUserProperties internalUserProperties;

    @Resource(name = "bCryptPasswordEncoder")
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Override
    @AuditLog(module = "INTERNAL_USER_MANAGEMENT", action = "RESET_INTERNAL_USER_PASSWORD")
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(InternalUserResetPasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long userId = request.getUserId();
        InternalUserEntity targetUser = internalUserOperationGuardService.validateOperationTarget(userId, "重置密码");

        String encodedPassword = bCryptPasswordEncoder.encode(internalUserProperties.getDefaultPassword());
        int affectedRows = internalUserDao.updatePassword(
                userId,
                encodedPassword,
                MustChangePasswordFlag.YES.getCode(),
                System.currentTimeMillis()
        );
        if (affectedRows <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "重置密码失败");
        }
        internalUserCacheCleanupService.cleanupAfterPasswordReset(
                userId,
                targetUser.getUsername(),
                targetUser.getPhone()
        );
    }
}
