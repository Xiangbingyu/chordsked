package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.utils.security.SecurityPrincipalUtils;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("internalUserOperationGuardService")
public class InternalUserOperationGuardServiceImpl implements InternalUserOperationGuardService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public InternalUserEntity validateOperationTarget(Long targetUserId, String actionName) {
        if (targetUserId == null || targetUserId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId必须大于0");
        }
        InternalUserEntity targetUser = internalUserDao.getById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户不存在");
        }
        if (!internalUserDao.existsAccessibleById(targetUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作该账号");
        }
        if (isProtectedUser(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统管理员账号不允许执行" + safeActionName(actionName));
        }
        Long currentUserId = SecurityPrincipalUtils.getCurrentUserId();
        if (currentUserId != null && Objects.equals(currentUserId, targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能在账号管理中操作自己的账号");
        }
        return targetUser;
    }

    @Override
    public boolean isProtectedUser(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .anyMatch(roleCode -> internalUserDao.hasRoleCode(userId, roleCode));
    }

    private String safeActionName(String actionName) {
        if (actionName == null || actionName.isBlank()) {
            return "该操作";
        }
        return actionName.trim();
    }
}
