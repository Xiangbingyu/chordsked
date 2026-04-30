package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserUpdateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.internaluser.InternalUserUpdateService;
import com.chordsked.backend.service.internaluser.InternalUserWriteValidator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service("internalUserUpdateService")
public class InternalUserUpdateServiceImpl implements InternalUserUpdateService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Resource(name = "internalUserWriteValidator")
    private InternalUserWriteValidator internalUserWriteValidator;

    @Override
    @AuditLog(module = "INTERNAL_USER_MANAGEMENT", action = "UPDATE_INTERNAL_USER")
    @Transactional(rollbackFor = Exception.class)
    public void update(InternalUserUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long userId = request.getUserId();
        InternalUserEntity existingUser = internalUserOperationGuardService.validateOperationTarget(userId, "修改");

        String phone = request.getPhone();
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone must not be blank");
        }
        String name = request.getName();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Integer dataScopeType = request.getDataScopeType();
        UserDataScopeType scopeType = UserDataScopeType.fromCode(dataScopeType);
        if (scopeType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据范围类型无效");
        }
        Long primaryOrgNodeId = request.getPrimaryOrgNodeId();
        if (primaryOrgNodeId == null || primaryOrgNodeId <= 0) {
            throw new IllegalArgumentException("primaryOrgNodeId must be greater than 0");
        }
        InternalUserEntity userByPhone = internalUserDao.getByPhone(phone.trim());
        if (userByPhone != null && !Objects.equals(userByPhone.getId(), userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号已存在");
        }

        List<Long> distinctRoleIds = internalUserWriteValidator.validateRoleIds(request.getRoleIds());
        List<Long> distinctOrgNodeIds = internalUserWriteValidator.validateOrgScopeNodeIds(
                request.getOrgScopeNodeIds(),
                primaryOrgNodeId,
                dataScopeType
        );
        OrgNodeEntity primaryOrgNode = orgNodeDao.getById(primaryOrgNodeId);

        long now = System.currentTimeMillis();
        InternalUserEntity user = new InternalUserEntity();
        user.setId(userId);
        user.setPhone(phone.trim());
        user.setName(name.trim());
        user.setAvatar(request.getAvatar());
        user.setDataScopeType(dataScopeType);
        user.setCampusId(primaryOrgNode == null ? null : primaryOrgNode.getCampusId());
        user.setOrgNodeId(primaryOrgNodeId);
        user.setUpdatedAt(now);
        int affectedRows = internalUserDao.updateById(user);
        if (affectedRows <= 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新账号失败");
        }

        userRoleDao.deleteByUserId(userId);
        List<UserRoleEntity> userRoles = distinctRoleIds.stream()
                .map(roleId -> buildUserRole(userId, roleId, now))
                .toList();
        userRoleDao.saveBatch(userRoles);

        userOrgScopeDao.deleteByUserId(userId);
        if (scopeType.isAssignedScope() && !distinctOrgNodeIds.isEmpty()) {
            userOrgScopeDao.saveBatch(distinctOrgNodeIds.stream()
                    .map(nodeId -> buildUserOrgScope(userId, nodeId, primaryOrgNodeId, now))
                    .toList());
        }

        internalUserCacheCleanupService.cleanupAfterProfileUpdated(
                userId,
                existingUser.getUsername(),
                existingUser.getPhone()
        );
    }

    private UserRoleEntity buildUserRole(Long userId, Long roleId, Long now) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        return userRole;
    }

    private UserOrgScopeEntity buildUserOrgScope(Long userId, Long orgNodeId, Long primaryOrgNodeId, Long now) {
        UserOrgScopeEntity userOrgScope = new UserOrgScopeEntity();
        userOrgScope.setUserId(userId);
        userOrgScope.setOrgNodeId(orgNodeId);
        userOrgScope.setIsPrimary(orgNodeId.equals(primaryOrgNodeId) ? 1 : 0);
        userOrgScope.setCreatedAt(now);
        userOrgScope.setUpdatedAt(now);
        return userOrgScope;
    }
}
