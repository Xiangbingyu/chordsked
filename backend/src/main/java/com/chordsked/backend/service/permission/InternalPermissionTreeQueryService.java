package com.chordsked.backend.service.permission;

import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;

import java.util.List;

public interface InternalPermissionTreeQueryService {
    List<InternalPermissionTreeQueryResultVO> listAdminPermissionTree();
}
