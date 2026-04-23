package com.chordsked.backend.service.role;

import com.chordsked.backend.model.dto.role.RoleCreateRequest;

public interface RoleCreateService {
    Long create(RoleCreateRequest request);
}
