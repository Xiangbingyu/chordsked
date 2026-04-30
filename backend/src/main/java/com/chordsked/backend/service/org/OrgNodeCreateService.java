package com.chordsked.backend.service.org;

import com.chordsked.backend.model.dto.org.OrgNodeCreateRequest;

public interface OrgNodeCreateService {
    Long create(OrgNodeCreateRequest request);
}
