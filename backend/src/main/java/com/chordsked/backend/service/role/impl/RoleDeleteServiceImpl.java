package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.audit.annotation.AuditLog;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.dao.RolePermissionDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.service.role.RoleDeleteService;
import com.chordsked.backend.service.role.RoleOperationGuardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("roleDeleteService")
public class RoleDeleteServiceImpl implements RoleDeleteService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "rolePermissionDao")
    private RolePermissionDao rolePermissionDao;

    @Resource(name = "roleOperationGuardService")
    private RoleOperationGuardService roleOperationGuardService;

    @Override
    @AuditLog(module = "ROLE_MANAGEMENT", action = "DELETE_ROLE")
    @Transactional(rollbackFor = Exception.class)
    public void delete(RoleDeleteRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Long roleId = request.getRoleId();
        RoleEntity role = roleOperationGuardService.validateOperationTarget(roleId, "删除");
        roleOperationGuardService.validateRoleCanBeDeleted(role);
        if (roleDao.countUserBinding(roleId) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色已绑定用户，不能删除");
        }
        rolePermissionDao.deleteByRoleId(roleId);
        roleDao.deleteById(roleId);
    }
}
