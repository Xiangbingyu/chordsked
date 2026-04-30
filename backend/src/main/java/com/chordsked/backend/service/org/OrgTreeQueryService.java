package com.chordsked.backend.service.org;

import com.chordsked.backend.model.vo.org.OrgTreeNodeVO;

import java.util.List;

public interface OrgTreeQueryService {
    List<OrgTreeNodeVO> listTree();
}
