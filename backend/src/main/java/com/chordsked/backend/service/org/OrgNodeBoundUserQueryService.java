package com.chordsked.backend.service.org;

import com.chordsked.backend.model.vo.org.OrgNodeBoundUserVO;

import java.util.List;

public interface OrgNodeBoundUserQueryService {
    List<OrgNodeBoundUserVO> listByNodeId(Long nodeId);
}
