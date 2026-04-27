package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import com.chordsked.backend.service.internaluser.InternalUserOperationGuardService;
import com.chordsked.backend.service.internaluser.InternalUserQueryService;
import com.chordsked.backend.utils.security.SecurityPrincipalUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service("internalUserQueryService")
public class InternalUserQueryServiceImpl implements InternalUserQueryService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Resource(name = "internalUserOperationGuardService")
    private InternalUserOperationGuardService internalUserOperationGuardService;

    @Override
    public PageResult<InternalUserQueryResultVO> list(InternalUserQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Integer page = request.getPage() == null ? 1 : request.getPage();
        Integer pageSize = request.getPageSize() == null ? appProperties.getDefaultPageSize() : request.getPageSize();
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        if (pageSize > appProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("pageSize too large");
        }
        if (request.getStatus() != null && InternalUserStatus.fromCode(request.getStatus()) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        request.setPage(page);
        request.setPageSize(pageSize);
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
        }

        long total = internalUserDao.countByQuery(request);
        List<InternalUserQueryResultVO> items = internalUserDao.listByQuery(request);
        if (items.isEmpty()) {
            return PageResult.of(total, items);
        }
        fillRoles(items);
        fillOperationFlags(items);
        return PageResult.of(total, items);
    }

    private void fillRoles(List<InternalUserQueryResultVO> items) {
        List<Long> userIds = items.stream()
                .map(InternalUserQueryResultVO::getId)
                .toList();
        List<UserRoleEntity> userRoles = userRoleDao.listByUserIds(userIds);
        Map<Long, List<Long>> roleIdsByUserId = new LinkedHashMap<>();
        Map<Long, List<String>> roleNamesByUserId = new LinkedHashMap<>();
        Map<Long, String> roleNameCache = new HashMap<>();

        for (UserRoleEntity userRole : userRoles) {
            Long userId = userRole.getUserId();
            Long roleId = userRole.getRoleId();
            roleIdsByUserId.computeIfAbsent(userId, key -> new ArrayList<>()).add(roleId);

            String roleName = roleNameCache.computeIfAbsent(roleId, this::resolveRoleName);
            if (roleName != null && !roleName.isBlank()) {
                roleNamesByUserId.computeIfAbsent(userId, key -> new ArrayList<>()).add(roleName);
            }
        }

        for (InternalUserQueryResultVO item : items) {
            item.setRoleIds(roleIdsByUserId.getOrDefault(item.getId(), List.of()));
            item.setRoleNames(roleNamesByUserId.getOrDefault(item.getId(), List.of()));
        }
    }

    private String resolveRoleName(Long roleId) {
        RoleEntity role = roleDao.getById(roleId);
        return role == null ? null : role.getName();
    }

    private void fillOperationFlags(List<InternalUserQueryResultVO> items) {
        Long currentUserId = SecurityPrincipalUtils.getCurrentUserId();
        for (InternalUserQueryResultVO item : items) {
            Long userId = item.getId();
            item.setCurrentUser(currentUserId != null && currentUserId.equals(userId));
            item.setSystemAccount(internalUserOperationGuardService.isProtectedUser(userId));
        }
    }
}
