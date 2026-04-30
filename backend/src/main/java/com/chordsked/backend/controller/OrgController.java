package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.model.dto.org.OrgNodeCreateRequest;
import com.chordsked.backend.model.dto.org.OrgNodeUpdateRequest;
import com.chordsked.backend.model.dto.org.OrgNodeUserBindUpdateRequest;
import com.chordsked.backend.model.vo.org.OrgAccountOptionVO;
import com.chordsked.backend.model.vo.org.OrgNodeBoundUserVO;
import com.chordsked.backend.model.vo.org.OrgNodeOptionVO;
import com.chordsked.backend.model.vo.org.OrgTreeNodeVO;
import com.chordsked.backend.service.org.OrgNodeCreateService;
import com.chordsked.backend.service.org.OrgNodeDeleteService;
import com.chordsked.backend.service.org.OrgNodeOptionQueryService;
import com.chordsked.backend.service.org.OrgNodeBoundUserQueryService;
import com.chordsked.backend.service.org.OrgNodeUpdateService;
import com.chordsked.backend.service.org.OrgNodeUserBindUpdateService;
import com.chordsked.backend.service.org.OrgAccountOptionQueryService;
import com.chordsked.backend.service.org.OrgTreeQueryService;
import com.chordsked.backend.utils.audit.AuditLogRequestUtils;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "组织树管理", description = "组织树相关接口")
public class OrgController {
    @Resource(name = "orgTreeQueryService")
    private OrgTreeQueryService orgTreeQueryService;

    @Resource(name = "orgNodeCreateService")
    private OrgNodeCreateService orgNodeCreateService;

    @Resource(name = "orgNodeOptionQueryService")
    private OrgNodeOptionQueryService orgNodeOptionQueryService;

    @Resource(name = "orgNodeUpdateService")
    private OrgNodeUpdateService orgNodeUpdateService;

    @Resource(name = "orgNodeDeleteService")
    private OrgNodeDeleteService orgNodeDeleteService;

    @Resource(name = "orgNodeBoundUserQueryService")
    private OrgNodeBoundUserQueryService orgNodeBoundUserQueryService;

    @Resource(name = "orgNodeUserBindUpdateService")
    private OrgNodeUserBindUpdateService orgNodeUserBindUpdateService;

    @Resource(name = "orgAccountOptionQueryService")
    private OrgAccountOptionQueryService orgAccountOptionQueryService;

    @GetMapping("/org-tree")
    @PreAuthorize("hasAuthority('admin:org:view')")
    @Operation(summary = "查询组织树", description = "查询完整组织树，供 org-tree 页面展示")
    public ApiResponse<List<OrgTreeNodeVO>> getOrgTree() {
        return ApiResponse.success(orgTreeQueryService.listTree());
    }

    @GetMapping("/org-nodes/options")
    @PreAuthorize("hasAuthority('admin:user:create') or hasAuthority('admin:user:update') or hasAuthority('admin:org:view')")
    @Operation(summary = "查询组织节点选项", description = "查询教务端账号编辑和组织树绑定使用的组织节点选项")
    public ApiResponse<List<OrgNodeOptionVO>> listOrgNodeOptions() {
        return ApiResponse.success(orgNodeOptionQueryService.list());
    }

    @PostMapping("/org-nodes")
    @PreAuthorize("hasAuthority('admin:org:create')")
    @Operation(summary = "创建组织节点", description = "创建 DEPT 或 GROUP 节点")
    public ApiResponse<Long> createOrgNode(
            @Valid @RequestBody OrgNodeCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return ApiResponse.success(orgNodeCreateService.create(request));
    }

    @PutMapping("/org-nodes/{nodeId}")
    @PreAuthorize("hasAuthority('admin:org:update')")
    @Operation(summary = "更新组织节点", description = "更新组织节点名称、编码、状态、排序和备注")
    public ApiResponse<Void> updateOrgNode(
            @PathVariable("nodeId") @Min(1) Long nodeId,
            @Valid @RequestBody OrgNodeUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        request.setNodeId(nodeId);
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        orgNodeUpdateService.update(request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/org-nodes/{nodeId}")
    @PreAuthorize("hasAuthority('admin:org:delete')")
    @Operation(summary = "删除组织节点", description = "删除无子节点且未绑定账号的组织节点")
    public ApiResponse<Void> deleteOrgNode(@PathVariable("nodeId") @Min(1) Long nodeId) {
        orgNodeDeleteService.delete(nodeId);
        return ApiResponse.success(null);
    }

    @GetMapping("/org-nodes/{nodeId}/users")
    @PreAuthorize("hasAuthority('admin:org:view') or hasAuthority('admin:org:assign_user')")
    @Operation(summary = "查询节点已绑定账号", description = "查询指定组织节点已绑定的教务账号列表")
    public ApiResponse<List<OrgNodeBoundUserVO>> listBoundUsers(@PathVariable("nodeId") @Min(1) Long nodeId) {
        return ApiResponse.success(orgNodeBoundUserQueryService.listByNodeId(nodeId));
    }

    @PutMapping("/org-nodes/{nodeId}/users")
    @PreAuthorize("hasAuthority('admin:org:assign_user')")
    @Operation(summary = "更新节点绑定账号", description = "覆盖更新指定组织节点绑定的教务账号列表")
    public ApiResponse<Void> updateBoundUsers(
            @PathVariable("nodeId") @Min(1) Long nodeId,
            @Valid @RequestBody OrgNodeUserBindUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        orgNodeUserBindUpdateService.updateBindings(nodeId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/org-accounts/options")
    @PreAuthorize("hasAuthority('admin:org:assign_user') or hasAuthority('admin:user:update')")
    @Operation(summary = "查询可绑定账号选项", description = "查询组织树可绑定的 ASSIGNED 教务账号选项")
    public ApiResponse<List<OrgAccountOptionVO>> listOrgAccountOptions() {
        return ApiResponse.success(orgAccountOptionQueryService.list());
    }
}
