package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserUpdateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.internaluser.InternalUserCacheCleanupService;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.internaluser.InternalUserUpdateService;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("internalUserUpdateService")
public class InternalUserUpdateServiceImpl implements InternalUserUpdateService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "userCampusDao")
    private UserCampusDao userCampusDao;

    @Resource(name = "internalUserCacheCleanupService")
    private InternalUserCacheCleanupService internalUserCacheCleanupService;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

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
        if (UserDataScopeType.fromCode(dataScopeType) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据范围类型无效");
        }
        Long primaryCampusId = request.getPrimaryCampusId();
        if (primaryCampusId == null || primaryCampusId <= 0) {
            throw new IllegalArgumentException("primaryCampusId must be greater than 0");
        }
        List<Long> roleIds = request.getRoleIds();
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleIds不能为空");
        }
        List<Long> campusIds = request.getCampusIds();
        if (campusIds == null || campusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "campusIds不能为空");
        }
        if (!campusIds.contains(primaryCampusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主校区必须在校区列表中");
        }

        InternalUserEntity userByPhone = internalUserDao.getByPhone(phone.trim());
        if (userByPhone != null && !Objects.equals(userByPhone.getId(), userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号已存在");
        }

        List<Long> distinctRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .filter(roleId -> roleId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctRoleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleIds不能为空");
        }
        String systemAdminRoleCode = resolveSystemAdminRoleCode();
        for (Long roleId : distinctRoleIds) {
            RoleEntity role = roleDao.getById(roleId);
            if (role == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在: " + roleId);
            }
            if (role.getStatusEnum() != RoleStatus.ENABLED) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色未启用: " + roleId);
            }
            if (systemAdminRoleCode.equals(StringNormalizeUtils.normalizeOrEmpty(role.getCode()))) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "系统管理员角色为固定系统角色，不能分配给普通账号");
            }
        }

        List<Long> distinctCampusIds = campusIds.stream()
                .filter(Objects::nonNull)
                .filter(campusId -> campusId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctCampusIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "campusIds不能为空");
        }
        if (!distinctCampusIds.contains(primaryCampusId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主校区必须在校区列表中");
        }
        for (Long campusId : distinctCampusIds) {
            if (campusDao.getById(campusId) == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "校区不存在: " + campusId);
            }
        }

        long now = System.currentTimeMillis();
        InternalUserEntity user = new InternalUserEntity();
        user.setId(userId);
        user.setPhone(phone.trim());
        user.setName(name.trim());
        user.setAvatar(request.getAvatar());
        user.setDataScopeType(dataScopeType);
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

        userCampusDao.deleteByUserId(userId);
        userCampusDao.saveBatch(distinctCampusIds, primaryCampusId, userId, now);

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

    private String resolveSystemAdminRoleCode() {
        String systemAdminRoleCode = StringNormalizeUtils.normalizeOrEmpty(
                roleProperties.getSystemAdminRoleCode()
        );
        return systemAdminRoleCode.isEmpty() ? "SYSTEM_ADMIN" : systemAdminRoleCode;
    }
}
