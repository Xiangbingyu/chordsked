package com.chordsked.backend.service.campus;

import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;

public interface CampusOperationGuardService {
    CampusDetailResultVO validateOperationTarget(Long campusId, String actionName);
}
