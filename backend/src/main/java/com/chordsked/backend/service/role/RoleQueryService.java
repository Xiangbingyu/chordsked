package com.chordsked.backend.service.role;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;

public interface RoleQueryService {
    PageResult<RoleQueryResultVO> list(RoleQueryRequest request);
}
