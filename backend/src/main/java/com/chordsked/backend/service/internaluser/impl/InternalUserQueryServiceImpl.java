package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.dao.InternalUserDao;
import com.chordsked.backend.dao.UserRoleDao;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.entity.UserRoleEntity;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import com.chordsked.backend.service.internaluser.InternalUserQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("internalUserQueryService")
public class InternalUserQueryServiceImpl implements InternalUserQueryService {
    @Resource(name = "internalUserDao")
    private InternalUserDao internalUserDao;

    @Resource(name = "userRoleDao")
    private UserRoleDao userRoleDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Override
    public PageResult<InternalUserQueryResultVO> list(InternalUserQueryRequest request) {
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
        if (request.getStatus() != null && InternalUserStatus.fromCode(request.getStatus()) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        request.setPage(page);
        request.setPageSize(pageSize);
        if (request.getKeyword() != null) {
            request.setKeyword(request.getKeyword().trim());
        }

        long total = internalUserDao.countByQuery(request);
        List<InternalUserQueryResultVO> items = internalUserDao.listByQuery(request);
        if (items.isEmpty()) {
            return PageResult.of(total, items);
        }
        fillRoleIds(items);
        return PageResult.of(total, items);
    }

    private void fillRoleIds(List<InternalUserQueryResultVO> items) {
        List<Long> userIds = items.stream()
                .map(InternalUserQueryResultVO::getId)
                .toList();
        List<UserRoleEntity> userRoles = userRoleDao.listByUserIds(userIds);
        Map<Long, List<Long>> roleIdsByUserId = userRoles.stream()
                .collect(Collectors.groupingBy(
                        UserRoleEntity::getUserId,
                        LinkedHashMap::new,
                        Collectors.mapping(UserRoleEntity::getRoleId, Collectors.toList())
                ));
        for (InternalUserQueryResultVO item : items) {
            item.setRoleIds(roleIdsByUserId.getOrDefault(item.getId(), List.of()));
        }
    }
}
