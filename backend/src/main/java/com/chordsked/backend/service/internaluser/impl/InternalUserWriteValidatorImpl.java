package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.OrgNodeDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.entity.OrgNodeEntity;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.enums.OrgNodeStatus;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.enums.UserDataScopeType;
import com.chordsked.backend.service.internaluser.InternalUserWriteValidator;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service("internalUserWriteValidator")
public class InternalUserWriteValidatorImpl implements InternalUserWriteValidator {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "orgNodeDao")
    private OrgNodeDao orgNodeDao;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public List<Long> validateRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleIds不能为空");
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

        Set<String> protectedRoleCodes = resolveProtectedRoleCodes();
        for (Long roleId : distinctRoleIds) {
            RoleEntity role = roleDao.getById(roleId);
            if (role == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在: " + roleId);
            }
            if (role.getStatusEnum() != RoleStatus.ENABLED) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色未启用: " + roleId);
            }
            if (protectedRoleCodes.contains(StringNormalizeUtils.normalizeOrEmpty(role.getCode()))) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "受保护角色不能分配给普通账号");
            }
        }
        return distinctRoleIds;
    }

    @Override
    public List<Long> validateOrgScopeNodeIds(List<Long> orgScopeNodeIds, Long primaryOrgNodeId, Integer dataScopeType) {
        if (primaryOrgNodeId == null || primaryOrgNodeId <= 0) {
            throw new IllegalArgumentException("primaryOrgNodeId must be greater than 0");
        }
        OrgNodeEntity primaryNode = orgNodeDao.getById(primaryOrgNodeId);
        if (primaryNode == null || primaryNode.getStatusEnum() != OrgNodeStatus.ENABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "主归属组织节点不存在或未启用");
        }

        UserDataScopeType scopeType = UserDataScopeType.fromCode(dataScopeType);
        if (scopeType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "数据范围类型无效");
        }
        if (!scopeType.isAssignedScope()) {
            return List.of();
        }
        if (orgScopeNodeIds == null || orgScopeNodeIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "orgScopeNodeIds不能为空");
        }

        List<Long> distinctNodeIds = orgScopeNodeIds.stream()
                .filter(Objects::nonNull)
                .filter(nodeId -> nodeId > 0)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (distinctNodeIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "orgScopeNodeIds不能为空");
        }
        for (Long nodeId : distinctNodeIds) {
            OrgNodeEntity node = orgNodeDao.getById(nodeId);
            if (node == null || node.getStatusEnum() != OrgNodeStatus.ENABLED) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "组织节点不存在或未启用: " + nodeId);
            }
        }
        return distinctNodeIds;
    }

    private Set<String> resolveProtectedRoleCodes() {
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
