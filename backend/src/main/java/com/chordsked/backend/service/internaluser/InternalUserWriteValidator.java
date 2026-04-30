package com.chordsked.backend.service.internaluser;

import java.util.List;

public interface InternalUserWriteValidator {
    List<Long> validateRoleIds(List<Long> roleIds);

    List<Long> validateOrgScopeNodeIds(List<Long> orgScopeNodeIds, Long primaryOrgNodeId, Integer dataScopeType);
}
