package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;

public interface InternalUserQueryService {
    PageResult<InternalUserQueryResultVO> list(InternalUserQueryRequest request);
}
