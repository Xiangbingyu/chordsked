package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;
import com.chordsked.backend.service.permission.InternalPermissionTreeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "权限管理", description = "RBAC 权限树接口")
public class PermissionController {
    @Resource(name = "internalPermissionTreeQueryService")
    private InternalPermissionTreeQueryService internalPermissionTreeQueryService;

    @GetMapping("/permissions/tree")
    @PreAuthorize("hasAuthority('admin:role:view') or hasAuthority('admin:role:assign_permission')")
    @Operation(summary = "查询固定权限树", description = "查询教务端固定权限树，供角色权限配置页面展示")
    public ApiResponse<List<InternalPermissionTreeQueryResultVO>> getPermissionTree() {
        return ApiResponse.success(internalPermissionTreeQueryService.listAdminPermissionTree());
    }
}
