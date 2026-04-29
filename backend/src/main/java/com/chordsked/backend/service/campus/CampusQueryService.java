package com.chordsked.backend.service.campus;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;

public interface CampusQueryService {
    PageResult<CampusQueryResultVO> list(CampusQueryRequest request);
}
