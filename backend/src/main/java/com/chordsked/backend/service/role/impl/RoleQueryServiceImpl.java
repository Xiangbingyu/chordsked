package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.config.properties.RoleProperties;
import com.chordsked.backend.dao.RoleDao;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import com.chordsked.backend.service.role.RoleQueryService;
import com.chordsked.backend.utils.normalize.StringNormalizeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        Set<String> protectedRoleCodes = resolveProtectedRoleCodes();
        long total = roleDao.countByQuery(request);
        if (protectedRoleCodes.isEmpty()) {
            return PageResult.of(total, roleDao.listByQuery(request));
        }
        long visibleTotal = Math.max(0L, total - countMatchedProtectedRoles(request, protectedRoleCodes));
        List<RoleQueryResultVO> items = loadVisibleItems(request, protectedRoleCodes);
        return PageResult.of(visibleTotal, items);
    }

    private List<RoleQueryResultVO> loadVisibleItems(RoleQueryRequest request, Set<String> protectedRoleCodes) {
        int pageSize = request.getPageSize() == null ? 0 : request.getPageSize();
        if (pageSize <= 0) {
            return List.of();
        }
        int targetStart = request.getOffset();
        int targetEnd = targetStart + pageSize;
        int scanPageSize = appProperties.getMaxPageSize();
        int scanPage = 1;
        List<RoleQueryResultVO> visibleItems = new ArrayList<>(targetEnd);
        while (visibleItems.size() < targetEnd) {
            RoleQueryRequest scanRequest = buildScanRequest(request, scanPage, scanPageSize);
            List<RoleQueryResultVO> batch = roleDao.listByQuery(scanRequest);
            if (batch.isEmpty()) {
                break;
            }
            batch.stream()
                    .filter(item -> !isProtectedRoleCode(item.getCode(), protectedRoleCodes))
                    .forEach(visibleItems::add);
            if (batch.size() < scanPageSize) {
                break;
            }
            scanPage++;
        }
        if (visibleItems.size() <= targetStart) {
            return List.of();
        }
        return List.copyOf(visibleItems.subList(targetStart, Math.min(targetEnd, visibleItems.size())));
    }

    private long countMatchedProtectedRoles(RoleQueryRequest request, Set<String> protectedRoleCodes) {
        return protectedRoleCodes.stream()
                .map(roleDao::getByCode)
                .filter(Objects::nonNull)
                .filter(role -> matchesQuery(role, request))
                .count();
    }

    private boolean matchesQuery(RoleEntity role, RoleQueryRequest request) {
        if (role == null) {
            return false;
        }
        if (request.getStatus() != null && !Objects.equals(role.getStatus(), request.getStatus())) {
            return false;
        }
        String keyword = request.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toUpperCase();
        String roleCode = role.getCode() == null ? "" : role.getCode().trim().toUpperCase();
        String roleName = role.getName() == null ? "" : role.getName().trim().toUpperCase();
        return roleCode.contains(normalizedKeyword) || roleName.contains(normalizedKeyword);
    }

    private RoleQueryRequest buildScanRequest(RoleQueryRequest source, int page, int pageSize) {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(source.getKeyword());
        request.setStatus(source.getStatus());
        return request;
    }

    private Set<String> resolveProtectedRoleCodes() {
        return roleProperties.getProtectedRoleCodes().stream()
                .filter(Objects::nonNull)
                .map(StringNormalizeUtils::normalizeOrEmpty)
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isProtectedRoleCode(String roleCode, Set<String> protectedRoleCodes) {
        return protectedRoleCodes.contains(StringNormalizeUtils.normalizeOrEmpty(roleCode));
    }
}
