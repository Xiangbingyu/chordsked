package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.dto.role.RoleUpdateRequest;
import com.chordsked.backend.model.enums.RoleStatus;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import com.chordsked.backend.service.role.RoleCreateService;
import com.chordsked.backend.service.role.RoleDeleteService;
import com.chordsked.backend.service.role.RoleDetailQueryService;
import com.chordsked.backend.service.role.RoleQueryService;
import com.chordsked.backend.service.role.RoleUpdateService;
import com.chordsked.backend.utils.audit.AuditLogRequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "角色管理", description = "角色权限相关接口")
public class RoleController {
    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Resource(name = "roleQueryService")
    private RoleQueryService roleQueryService;

    @Resource(name = "roleDetailQueryService")
    private RoleDetailQueryService roleDetailQueryService;

    @Resource(name = "roleCreateService")
    private RoleCreateService roleCreateService;

    @Resource(name = "roleDeleteService")
    private RoleDeleteService roleDeleteService;

    @Resource(name = "roleUpdateService")
    private RoleUpdateService roleUpdateService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('admin:role:view')")
    @Operation(summary = "分页查询角色列表", description = "按条件分页查询角色列表，返回角色基础信息、权限数量与用户数量")
    public ApiResponse<PageResult<RoleQueryResultVO>> listRoles(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "每页条数")
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "关键词（角色名称/角色编码）")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "角色状态")
            @RequestParam(name = "status", required = false) Integer status
    ) {
        Integer currentPage = page == null ? 1 : page;
        Integer currentPageSize = pageSize == null ? appProperties.getDefaultPageSize() : pageSize;
        if (currentPage < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (currentPageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        if (currentPageSize > appProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("pageSize too large");
        }
        if (status != null && RoleStatus.fromCode(status) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        return ApiResponse.success(roleQueryService.list(buildRoleQueryRequest(currentPage, currentPageSize, keyword, status)));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('admin:role:create')")
    @Operation(summary = "创建角色", description = "创建角色并同时绑定权限配置")
    public ApiResponse<Long> createRole(@Valid @RequestBody RoleCreateRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.success(roleCreateService.create(buildRoleCreateRequest(request, httpServletRequest)));
    }

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('admin:role:view')")
    @Operation(summary = "查询角色详情", description = "按角色ID查询角色完整详情，包含基础信息、统计信息、已分配权限和完整权限树")
    public ApiResponse<RoleDetailQueryResultVO> getRoleDetail(
            @Parameter(description = "角色ID")
            @PathVariable("roleId") @Min(1) Long roleId
    ) {
        return ApiResponse.success(roleDetailQueryService.getDetail(buildRoleDetailQueryRequest(roleId)));
    }

    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('admin:role:update')")
    @Operation(summary = "编辑角色", description = "更新角色名称、描述、状态和权限配置")
    public ApiResponse<Void> updateRole(
            @Parameter(description = "角色ID")
            @PathVariable("roleId") @Min(1) Long roleId,
            @Valid @RequestBody RoleUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        roleUpdateService.update(buildRoleUpdateRequest(roleId, request, httpServletRequest));
        return ApiResponse.success(null);
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority('admin:role:delete')")
    @Operation(summary = "删除角色", description = "删除非系统预置且未绑定用户的角色，并清理角色权限关联")
    public ApiResponse<Void> deleteRole(
            @Parameter(description = "角色ID")
            @PathVariable("roleId") @Min(1) Long roleId,
            HttpServletRequest httpServletRequest
    ) {
        roleDeleteService.delete(buildRoleDeleteRequest(roleId, httpServletRequest));
        return ApiResponse.success(null);
    }

    private RoleQueryRequest buildRoleQueryRequest(Integer page, Integer pageSize, String keyword, Integer status) {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setStatus(status);
        return request;
    }

    private RoleCreateRequest buildRoleCreateRequest(RoleCreateRequest source, HttpServletRequest httpServletRequest) {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setCode(source.getCode());
        request.setName(source.getName());
        request.setDescription(source.getDescription());
        request.setStatus(source.getStatus());
        request.setPermissionIds(source.getPermissionIds());
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }

    private RoleDetailQueryRequest buildRoleDetailQueryRequest(Long roleId) {
        RoleDetailQueryRequest request = new RoleDetailQueryRequest();
        request.setRoleId(roleId);
        return request;
    }

    private RoleUpdateRequest buildRoleUpdateRequest(Long roleId, RoleUpdateRequest source, HttpServletRequest httpServletRequest) {
        RoleUpdateRequest request = new RoleUpdateRequest();
        request.setRoleId(roleId);
        request.setCode(source.getCode());
        request.setName(source.getName());
        request.setDescription(source.getDescription());
        request.setStatus(source.getStatus());
        request.setPermissionIds(source.getPermissionIds());
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }

    private RoleDeleteRequest buildRoleDeleteRequest(Long roleId, HttpServletRequest httpServletRequest) {
        RoleDeleteRequest request = new RoleDeleteRequest();
        request.setRoleId(roleId);
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }
}
