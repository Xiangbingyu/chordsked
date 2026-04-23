package com.chordsked.backend.service.permission.impl;

import com.chordsked.backend.dao.PermissionDao;
import com.chordsked.backend.model.entity.PermissionEntity;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.PermissionStatus;
import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;
import com.chordsked.backend.service.permission.InternalPermissionTreeQueryService;
import com.chordsked.backend.utils.tree.PermissionTreeBuilder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service("internalPermissionTreeQueryService")
public class InternalPermissionTreeQueryServiceImpl implements InternalPermissionTreeQueryService {
    @Resource(name = "permissionDao")
    private PermissionDao permissionDao;

    @Resource(name = "permissionTreeBuilder")
    private PermissionTreeBuilder permissionTreeBuilder;

    @Override
    public List<InternalPermissionTreeQueryResultVO> listAdminPermissionTree() {
        List<PermissionEntity> permissions = permissionDao.listByUserType(AccountUserType.ADMIN.getCode());
        List<PermissionEntity> treePermissions = permissions.stream()
                .filter(Objects::nonNull)
                .filter(this::isEnabled)
                .toList();
        return permissionTreeBuilder.build(treePermissions);
    }

    private boolean isEnabled(PermissionEntity permission) {
        return PermissionStatus.ENABLED.equals(permission.getStatusEnum());
    }
}
