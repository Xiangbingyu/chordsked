package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserStatusUpdateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.internaluser.InternalUserStatusUpdateService;
import com.chordsked.backend.utils.security.SecurityPrincipalUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("internalUserStatusUpdateService")
public class InternalUserStatusUpdateServiceImpl implements InternalUserStatusUpdateService {
    private static final String ADMIN_ROLE_CODE = "ADMIN";

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Override
    @AuditLog(module = "INTERNAL_USER_MANAGEMENT", action = "UPDATE_INTERNAL_USER_STATUS")
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(InternalUserStatusUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long userId = request.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId必须大于0");
        }
        InternalUserStatus targetStatus = InternalUserStatus.fromCode(request.getStatus());
        if (targetStatus == null || targetStatus == InternalUserStatus.DELETED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status is invalid");
        }
        InternalUserEntity targetUser = internalUserDao.getById(userId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户不存在");
        }
        if (!internalUserDao.existsAccessibleById(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作该账号");
        }
        Long operatorUserId = SecurityPrincipalUtils.getCurrentUserId();
        if (operatorUserId != null
                && targetStatus == InternalUserStatus.DISABLED
                && operatorUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能禁用自己");
        }
        if (targetStatus == InternalUserStatus.DISABLED
                && internalUserDao.hasRoleCode(userId, ADMIN_ROLE_CODE)) {
            Long enabledAdminCount = internalUserDao.countEnabledUsersByRoleCode(ADMIN_ROLE_CODE);
            if (enabledAdminCount != null && enabledAdminCount <= 1L) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "至少保留一个启用的管理员账号");
            }
        }

        int affectedRows = internalUserDao.updateStatus(userId, targetStatus.getCode(), System.currentTimeMillis());
        if (affectedRows <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新账号状态失败");
        }
        if (targetStatus == InternalUserStatus.DISABLED) {
            internalUserCacheCleanupService.cleanupAfterStatusDisabled(
                    userId,
                    targetUser.getUsername(),
                    targetUser.getPhone()
            );
            return;
        }
        internalUserCacheCleanupService.cleanupAfterStatusEnabled(
                userId,
                targetUser.getUsername(),
                targetUser.getPhone()
        );
    }
}
