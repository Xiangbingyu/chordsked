package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.dto.internaluser.InternalUserCreateRequest;

public interface InternalUserCreateService {
    Long create(InternalUserCreateRequest request);
}
