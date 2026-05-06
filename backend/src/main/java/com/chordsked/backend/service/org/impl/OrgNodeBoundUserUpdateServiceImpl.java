package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.org.OrgNodeUserBindUpdateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.org.OrgNodeBoundUserUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service("orgNodeBoundUserUpdateService")
public class OrgNodeBoundUserUpdateServiceImpl implements OrgNodeBoundUserUpdateService {
    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Override
    @AuditLog(module = "ORG_MANAGEMENT", action = "UPDATE_ORG_NODE_BINDINGS")
    @Transactional(rollbackFor = Exception.class)
    public void updateBindings(Long nodeId, OrgNodeUserBindUpdateRequest request) {
        if (nodeId == null || nodeId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点ID无效");
        }
        if (request == null || request.getUserIds() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "绑定账号列表不能为空");
        }
        OrgNodeEntity node = orgNodeDao.getById(nodeId);
        if (node == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "节点不存在");
        }
        Set<Long> nextUserIds = new LinkedHashSet<>(request.getUserIds());
        List<UserOrgScopeEntity> currentScopes = userOrgScopeDao.listByOrgNodeId(nodeId);
        Set<Long> currentUserIds = currentScopes.stream()
                .map(UserOrgScopeEntity::getUserId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        long now = System.currentTimeMillis();
        for (Long userId : nextUserIds) {
            InternalUserEntity user = internalUserDao.getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "账号不存在: " + userId);
            }
            if (user.getDataScopeTypeEnum() == null || !user.getDataScopeTypeEnum().isAssignedScope()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "仅 ASSIGNED 用户可绑定组织节点");
            }
            if (currentUserIds.contains(userId)) {
                continue;
            }
            Long currentPrimaryOrgNodeId = userOrgScopeDao.getPrimaryOrgNodeIdByUserId(userId);
            boolean shouldInitializePrimary = currentPrimaryOrgNodeId == null || currentPrimaryOrgNodeId <= 0;
            UserOrgScopeEntity scope = new UserOrgScopeEntity();
            scope.setUserId(userId);
            scope.setOrgNodeId(nodeId);
            scope.setIsPrimary(shouldInitializePrimary ? 1 : 0);
            scope.setCreatedAt(now);
            scope.setUpdatedAt(now);
            userOrgScopeDao.saveBatch(List.of(scope));

            InternalUserEntity updateUser = new InternalUserEntity();
            updateUser.setId(userId);
            updateUser.setCampusId(node.getCampusId());
            if (shouldInitializePrimary) {
                updateUser.setOrgNodeId(nodeId);
            }
            updateUser.setPhone(user.getPhone());
            updateUser.setName(user.getName());
            updateUser.setAvatar(user.getAvatar());
            updateUser.setDataScopeType(UserDataScopeType.ASSIGNED.getCode());
            updateUser.setUpdatedAt(now);
            internalUserDao.updateById(updateUser);
            internalUserCacheCleanupService.cleanupAfterProfileUpdated(userId, user.getUsername(), user.getPhone());
        }

        for (Long userId : currentUserIds) {
            if (nextUserIds.contains(userId)) {
                continue;
            }
            userOrgScopeDao.deleteByUserIdAndOrgNodeId(userId, nodeId);
            InternalUserEntity user = internalUserDao.getById(userId);
            if (user == null) {
                continue;
            }
            Long primaryOrgNodeId = userOrgScopeDao.getPrimaryOrgNodeIdByUserId(userId);
            if (primaryOrgNodeId != null && primaryOrgNodeId > 0) {
                continue;
            }
            List<UserOrgScopeEntity> remainingScopes = userOrgScopeDao.listByUserId(userId);
            if (remainingScopes.isEmpty()) {
                continue;
            }
            UserOrgScopeEntity fallbackPrimary = remainingScopes.get(0);
            OrgNodeEntity fallbackNode = orgNodeDao.getById(fallbackPrimary.getOrgNodeId());
            if (fallbackNode == null) {
                continue;
            }
            userOrgScopeDao.deleteByUserId(userId);
            List<UserOrgScopeEntity> rebuiltScopes = remainingScopes.stream()
                    .map(scope -> buildScope(scope.getUserId(), scope.getOrgNodeId(), scope.getOrgNodeId().equals(fallbackPrimary.getOrgNodeId()) ? 1 : 0, now))
                    .toList();
            userOrgScopeDao.saveBatch(rebuiltScopes);

            InternalUserEntity updateUser = new InternalUserEntity();
            updateUser.setId(userId);
            updateUser.setCampusId(fallbackNode.getCampusId());
            updateUser.setOrgNodeId(fallbackPrimary.getOrgNodeId());
            updateUser.setPhone(user.getPhone());
            updateUser.setName(user.getName());
            updateUser.setAvatar(user.getAvatar());
            updateUser.setDataScopeType(UserDataScopeType.ASSIGNED.getCode());
            updateUser.setUpdatedAt(now);
            internalUserDao.updateById(updateUser);
            internalUserCacheCleanupService.cleanupAfterProfileUpdated(userId, user.getUsername(), user.getPhone());
        }
    }

    private UserOrgScopeEntity buildScope(Long userId, Long orgNodeId, Integer isPrimary, long now) {
        UserOrgScopeEntity scope = new UserOrgScopeEntity();
        scope.setUserId(userId);
        scope.setOrgNodeId(orgNodeId);
        scope.setIsPrimary(isPrimary);
        scope.setCreatedAt(now);
        scope.setUpdatedAt(now);
        return scope;
    }
}
