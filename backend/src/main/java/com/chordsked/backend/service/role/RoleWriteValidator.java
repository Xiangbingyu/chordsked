package com.chordsked.backend.service.role;

import java.util.List;

public interface RoleWriteValidator {
    String validateAndNormalizeCode(String code);

    String validateAndNormalizeName(String name);

    void validateDescription(String description);

    Integer validateStatus(Integer status);

    List<Long> validatePermissionIds(List<Long> permissionIds);

    void validateRoleCodeForCreate(String roleCode);

    void validateRoleCodeForUpdate(String roleCode);
}
