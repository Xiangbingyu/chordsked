package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import com.chordsked.backend.service.role.RoleQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("roleQueryService")
public class RoleQueryServiceImpl implements RoleQueryService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Override
    public PageResult<RoleQueryResultVO> list(RoleQueryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Integer page = request.getPage() == null ? 1 : request.getPage();
        Integer pageSize = request.getPageSize() == null ? appProperties.getDefaultPageSize() : request.getPageSize();
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        if (pageSize > appProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("pageSize too large");
        }
        if (request.getStatus() != null && RoleStatus.fromCode(request.getStatus()) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        request.setPage(page);
        request.setPageSize(pageSize);
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
        }
        long total = roleDao.countByQuery(request);
        List<RoleQueryResultVO> items = roleDao.listByQuery(request);
        return PageResult.of(total, items);
    }
}
