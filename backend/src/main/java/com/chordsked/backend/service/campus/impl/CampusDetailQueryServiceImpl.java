package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusDetailQueryService;
import com.chordsked.backend.service.campus.CampusOperationGuardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service("campusDetailQueryService")
public class CampusDetailQueryServiceImpl implements CampusDetailQueryService {
    @Resource(name = "campusOperationGuardService")
    private CampusOperationGuardService campusOperationGuardService;

    @Override
    public CampusDetailResultVO getDetail(CampusDetailQueryRequest request) {
        if (request == null || request.getCampusId() == null || request.getCampusId() <= 0) {
            throw new IllegalArgumentException("campusId必须大于0");
        }
        return campusOperationGuardService.validateOperationTarget(request.getCampusId(), "查看详情");
    }
}
