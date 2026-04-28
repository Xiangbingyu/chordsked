package com.chordsked.backend.service.role;

import com.chordsked.backend.model.entity.RoleEntity;

public interface RoleOperationGuardService {
    RoleEntity validateOperationTarget(Long roleId, String actionName);

    void validateRoleCanBeUpdated(RoleEntity role);

    void validateRoleCanBeDeleted(RoleEntity role);

    boolean isProtectedRoleCode(String roleCode);
}
