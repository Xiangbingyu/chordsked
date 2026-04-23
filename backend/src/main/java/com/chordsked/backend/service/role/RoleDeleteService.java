package com.chordsked.backend.service.role;

import com.chordsked.backend.model.dto.role.RoleDeleteRequest;

public interface RoleDeleteService {
    void delete(RoleDeleteRequest request);
}
