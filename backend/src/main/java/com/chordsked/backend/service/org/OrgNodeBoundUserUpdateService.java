package com.chordsked.backend.service.org;

import com.chordsked.backend.model.dto.org.OrgNodeUserBindUpdateRequest;

public interface OrgNodeBoundUserUpdateService {
    void updateBindings(Long nodeId, OrgNodeUserBindUpdateRequest request);
}
