package com.chordsked.backend.service.role;

import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;

public interface RoleDetailQueryService {
    RoleDetailQueryResultVO getDetail(RoleDetailQueryRequest request);
}
