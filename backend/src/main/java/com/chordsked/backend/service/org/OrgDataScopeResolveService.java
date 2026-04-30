package com.chordsked.backend.service.org;

import java.util.List;

public interface OrgDataScopeResolveService {
    OrgDataScopeResult resolveByUserId(Long userId);

    record OrgDataScopeResult(
            Long primaryOrgNodeId,
            List<Long> authorizedOrgNodeIds,
            List<Long> authorizedCampusIds
    ) {
    }
}
