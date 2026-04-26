package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.config.properties.InternalUserProperties;
import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserCreateRequest;
import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.internaluser.InternalUserCreateService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Service("internalUserCreateService")
public class InternalUserCreateServiceImpl implements InternalUserCreateService {
    private static final Logger logger = LoggerFactory.getLogger(InternalUserCreateServiceImpl.class);

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

    @Resource(name = "internalUserProperties")
    private InternalUserProperties internalUserProperties;

    @Resource(name = "bCryptPasswordEncoder")
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @AuditLog(module = "INTERNAL_USER_MANAGEMENT", action = "CREATE_INTERNAL_USER")
    @Transactional(rollbackFor = Exception.class)
    public Long create(InternalUserCreateRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("request must not be null");
            }
            String username = request.getUsername();
            if (username == null || username.isBlank()) {
                throw new IllegalArgumentException("username must not be blank");
            }
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

            if (internalUserDao.getByUsername(username.trim()) != null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
            }
            if (internalUserDao.getByPhone(phone.trim()) != null) {
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

            for (Long roleId : distinctRoleIds) {
                RoleEntity role = roleDao.getById(roleId);
                if (role == null) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在: " + roleId);
                }
                if (role.getStatusEnum() != RoleStatus.ENABLED) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "角色未启用: " + roleId);
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
            String defaultPassword = internalUserProperties.getDefaultPassword();
            String encodedPassword = bCryptPasswordEncoder.encode(defaultPassword);

            InternalUserEntity user = new InternalUserEntity();
            user.setUsername(username.trim());
            user.setPassword(encodedPassword);
            user.setPhone(phone.trim());
            user.setName(name.trim());
            user.setAvatar(request.getAvatar());
            user.setStatus(InternalUserStatus.ENABLED.getCode());
            user.setMustChangePassword(MustChangePasswordFlag.YES.getCode());
            user.setDataScopeType(dataScopeType);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            internalUserDao.save(user);

            List<UserRoleEntity> userRoles = distinctRoleIds.stream()
                    .map(roleId -> buildUserRole(user.getId(), roleId, now))
                    .toList();
            userRoleDao.saveBatch(userRoles);

            userCampusDao.saveBatch(distinctCampusIds, primaryCampusId, user.getId(), now);
            return user.getId();
        } catch (RuntimeException exception) {
            logger.warn(
                    "Internal user create failed, username={}, phone={}, reason={}",
                    request == null ? null : request.getUsername(),
                    request == null ? null : request.getPhone(),
                    exception.getMessage(),
                    exception
            );
            throw exception;
        }
    }

    private UserRoleEntity buildUserRole(Long userId, Long roleId, Long now) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        return userRole;
    }
}
