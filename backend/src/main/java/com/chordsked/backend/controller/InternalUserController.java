package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.idempotent.annotation.Idempotent;
import com.chordsked.backend.model.dto.internaluser.InternalUserCreateRequest;
import com.chordsked.backend.model.dto.internaluser.InternalUserQueryRequest;
import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.vo.internaluser.InternalUserQueryResultVO;
import com.chordsked.backend.service.internaluser.InternalUserCreateService;
import com.chordsked.backend.service.internaluser.InternalUserQueryService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "教务端账号管理", description = "教务端账号相关接口")
public class InternalUserController {
    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Resource(name = "internalUserQueryService")
    private InternalUserQueryService internalUserQueryService;

    @Resource(name = "internalUserCreateService")
    private InternalUserCreateService internalUserCreateService;

    @GetMapping("/internal-users")
    @PreAuthorize("hasAuthority('admin:user:view')")
    @Operation(summary = "分页查询教务端账号", description = "按条件分页查询教务端账号列表，并根据当前登录用户的数据权限范围自动过滤结果")
    public ApiResponse<PageResult<InternalUserQueryResultVO>> listInternalUsers(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "每页条数")
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "关键词（用户名/姓名/手机号）")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "账号状态")
            @RequestParam(name = "status", required = false) Integer status,
            @Parameter(description = "角色ID")
            @RequestParam(name = "roleId", required = false) @Min(1) Long roleId,
            @Parameter(description = "校区ID")
            @RequestParam(name = "campusId", required = false) @Min(1) Long campusId
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
        if (status != null && InternalUserStatus.fromCode(status) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        return ApiResponse.success(internalUserQueryService.list(
                buildInternalUserQueryRequest(currentPage, currentPageSize, keyword, status, roleId, campusId)
        ));
    }

    private InternalUserQueryRequest buildInternalUserQueryRequest(
            Integer page,
            Integer pageSize,
            String keyword,
            Integer status,
            Long roleId,
            Long campusId
    ) {
        InternalUserQueryRequest request = new InternalUserQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setStatus(status);
        request.setRoleId(roleId);
        request.setCampusId(campusId);
        return request;
    }

    @PostMapping("/internal-users")
    @Idempotent(expireSeconds = 5, message = "请勿重复提交创建请求")
    @PreAuthorize("hasAuthority('admin:user:create')")
    @Operation(summary = "创建教务端账号", description = "创建新的教务端账号，同时绑定角色和校区")
    public ApiResponse<Long> createInternalUser(
            @Valid @RequestBody InternalUserCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Long userId = internalUserCreateService.create(buildInternalUserCreateRequest(request, httpServletRequest));
        return ApiResponse.success(userId);
    }

    private InternalUserCreateRequest buildInternalUserCreateRequest(
            InternalUserCreateRequest source,
            HttpServletRequest httpServletRequest
    ) {
        InternalUserCreateRequest request = new InternalUserCreateRequest();
        request.setUsername(source.getUsername());
        request.setPhone(source.getPhone());
        request.setName(source.getName());
        request.setAvatar(source.getAvatar());
        request.setRoleIds(source.getRoleIds());
        request.setCampusIds(source.getCampusIds());
        request.setPrimaryCampusId(source.getPrimaryCampusId());
        request.setDataScopeType(source.getDataScopeType());
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }
}
