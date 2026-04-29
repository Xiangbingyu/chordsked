package com.chordsked.backend.service.campus;

import com.chordsked.backend.model.dto.campus.CampusDeleteRequest;

public interface CampusDeleteService {
    void delete(CampusDeleteRequest request);
}
