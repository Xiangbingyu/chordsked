package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.exception.ErrorCode;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import com.chordsked.backend.service.role.RoleQueryService;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("roleQueryService")
public class RoleQueryServiceImpl implements RoleQueryService {
    @Resource(name = "roleDao")
    private RoleDao roleDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Resource(name = "roleProperties")
    private RoleProperties roleProperties;

    @Override
    public PageResult<RoleQueryResultVO> list(RoleQueryRequest request) {
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
        if (request.getStatus() != null && RoleStatus.fromCode(request.getStatus()) == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值无效");
        }
        request.setPage(page);
        request.setPageSize(pageSize);
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
        }
        Set<String> protectedRoleCodes = resolveProtectedRoleCodes();
        List<String> protectedRoleCodeList = List.copyOf(protectedRoleCodes);
        long total = roleDao.countByQuery(request, protectedRoleCodeList);
        List<RoleQueryResultVO> items = roleDao.listByQuery(request, protectedRoleCodeList);
        return PageResult.of(total, items);
    }

    private Set<String> resolveProtectedRoleCodes() {
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
