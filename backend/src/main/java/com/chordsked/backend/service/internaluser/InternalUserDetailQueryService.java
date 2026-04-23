package com.chordsked.backend.service.internaluser;

import com.chordsked.backend.model.dto.internaluser.InternalUserDetailQueryRequest;
import com.chordsked.backend.model.vo.internaluser.InternalUserDetailResultVO;

public interface InternalUserDetailQueryService {
    InternalUserDetailResultVO getDetail(InternalUserDetailQueryRequest request);
}
