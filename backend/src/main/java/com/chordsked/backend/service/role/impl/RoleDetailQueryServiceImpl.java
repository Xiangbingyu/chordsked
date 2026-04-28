package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.entity.RolePermissionEntity;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.service.permission.InternalPermissionTreeQueryService;
import com.chordsked.backend.service.role.RoleDetailQueryService;
import com.chordsked.backend.service.role.RoleOperationGuardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service("roleDetailQueryService")
public class RoleDetailQueryServiceImpl implements RoleDetailQueryService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Resource(name = "internalPermissionTreeQueryService")
    private InternalPermissionTreeQueryService internalPermissionTreeQueryService;

    @Resource(name = "roleOperationGuardService")
    private RoleOperationGuardService roleOperationGuardService;

    @Override
    public RoleDetailQueryResultVO getDetail(RoleDetailQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long roleId = request.getRoleId();
        if (roleId == null || roleId <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "roleId必须大于0");
        }
        RoleDetailQueryResultVO result = roleDao.getDetailById(roleId);
        if (result == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在");
        }
        if (roleOperationGuardService.isProtectedRoleCode(result.getCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统保护角色不支持查看详情");
        }
        List<Long> permissionIds = rolePermissionDao.listByRoleId(roleId).stream()
                .filter(Objects::nonNull)
                .map(RolePermissionEntity::getPermissionId)
                .filter(Objects::nonNull)
                .toList();
        result.setPermissionIds(permissionIds);
        result.setPermissionTree(internalPermissionTreeQueryService.listAdminPermissionTree());
        return result;
    }
}
