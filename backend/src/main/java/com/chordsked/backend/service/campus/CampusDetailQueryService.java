package com.chordsked.backend.service.campus;

import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;

public interface CampusDetailQueryService {
    CampusDetailResultVO getDetail(CampusDetailQueryRequest request);
}
