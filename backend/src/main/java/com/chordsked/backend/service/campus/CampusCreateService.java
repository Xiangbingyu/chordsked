package com.chordsked.backend.service.campus;

import com.chordsked.backend.model.dto.campus.CampusCreateRequest;

public interface CampusCreateService {
    Long create(CampusCreateRequest request);
}
