package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.UserOrgScopeDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.internaluser.InternalUserDetailQueryRequest;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.entity.UserOrgScopeEntity;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;
import com.chordsked.backend.service.internaluser.InternalUserDetailQueryService;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.security.AuthorityCodeService;
import com.chordsked.backend.utils.security.SecurityPrincipalUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("internalUserDetailQueryService")
public class InternalUserDetailQueryServiceImpl implements InternalUserDetailQueryService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Resource(name = "userOrgScopeDao")
    private UserOrgScopeDao userOrgScopeDao;

    @Resource(name = "authorityCodeService")
    private AuthorityCodeService authorityCodeService;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Override
    public InternalUserDetailResultVO getDetail(InternalUserDetailQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long userId = request.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "userId必须大于0");
        }
        InternalUserDetailResultVO detail = internalUserDao.getAccessibleDetailById(userId);
        if (detail == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看该账号");
        }
        List<UserRoleEntity> userRoles = userRoleDao.listByUserIds(List.of(userId));
        detail.setRoleIds(userRoles.stream().map(UserRoleEntity::getRoleId).toList());
        detail.setRoleNames(userRoles.stream()
                .map(UserRoleEntity::getRoleId)
                .map(this::resolveRoleName)
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .toList());
        List<UserOrgScopeEntity> userOrgScopes = userOrgScopeDao.listByUserId(userId);
        detail.setOrgScopeNodeIds(userOrgScopes.stream().map(UserOrgScopeEntity::getOrgNodeId).toList());
        OrgNodeEntity orgNode = detail.getOrgNodeId() == null ? null : orgNodeDao.getById(detail.getOrgNodeId());
        if (orgNode != null) {
            detail.setOrgNodeName(orgNode.getName());
            detail.setOrgNodeType(orgNode.getNodeType());
        }
        detail.setPermissionCodes(authorityCodeService.getAuthorityCodes(AccountUserType.ADMIN, userId));
        Long currentUserId = SecurityPrincipalUtils.getCurrentUserId();
        detail.setCurrentUser(currentUserId != null && currentUserId.equals(userId));
        detail.setSystemAccount(internalUserOperationGuardService.isProtectedUser(userId));
        return detail;
    }

    private String resolveRoleName(Long roleId) {
        RoleEntity role = roleDao.getById(roleId);
        return role == null ? null : role.getName();
    }
}
