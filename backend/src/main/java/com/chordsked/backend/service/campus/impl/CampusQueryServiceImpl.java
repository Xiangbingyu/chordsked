package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.enums.CampusStatus;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;
import com.chordsked.backend.service.campus.CampusQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("campusQueryService")
public class CampusQueryServiceImpl implements CampusQueryService {
    @Resource(name = "campusDao")
    private com.chordsked.backend.dao.CampusDao campusDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Override
    public PageResult<CampusQueryResultVO> list(CampusQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求参数不能为空");
        }
        Integer page = request.getPage() == null ? 1 : request.getPage();
        Integer pageSize = request.getPageSize() == null ? appProperties.getDefaultPageSize() : request.getPageSize();
        if (page < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "页码必须大于等于1");
        }
        if (pageSize < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "每页条数必须大于等于1");
        }
        if (pageSize > appProperties.getMaxPageSize()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "每页条数超出限制");
        }
        if (request.getStatus() != null && CampusStatus.fromCode(request.getStatus()) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        request.setPage(page);
        request.setPageSize(pageSize);
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
        }
        long total = campusDao.countByQuery(request);
        List<CampusQueryResultVO> items = campusDao.listByQuery(request);
        return PageResult.of(total, items);
    }
}
