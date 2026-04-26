package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserStatusUpdateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.internaluser.InternalUserStatusUpdateService;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("internalUserStatusUpdateService")
public class InternalUserStatusUpdateServiceImpl implements InternalUserStatusUpdateService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

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
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        InternalUserEntity targetUser = internalUserOperationGuardService.validateOperationTarget(userId, "修改状态");
        String systemAdminRoleCode = resolveSystemAdminRoleCode();
        if (targetStatus == InternalUserStatus.DISABLED
                && internalUserDao.hasRoleCode(userId, systemAdminRoleCode)) {
            Long enabledAdminCount = internalUserDao.countEnabledUsersByRoleCode(systemAdminRoleCode);
            if (enabledAdminCount != null && enabledAdminCount <= 1L) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "至少保留一个启用的系统管理员账号");
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

    private String resolveSystemAdminRoleCode() {
        String systemAdminRoleCode = StringNormalizeUtils.normalizeOrEmpty(
                roleProperties.getSystemAdminRoleCode()
        );
        return systemAdminRoleCode.isEmpty() ? "SYSTEM_ADMIN" : systemAdminRoleCode;
    }
}

