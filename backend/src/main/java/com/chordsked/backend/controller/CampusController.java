package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.model.dto.campus.CampusCreateRequest;
import com.chordsked.backend.model.dto.campus.CampusDeleteRequest;
import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.dto.campus.CampusUpdateRequest;
import com.chordsked.backend.model.enums.CampusStatus;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;
import com.chordsked.backend.service.campus.CampusCreateService;
import com.chordsked.backend.service.campus.CampusDeleteService;
import com.chordsked.backend.service.campus.CampusDetailQueryService;
import com.chordsked.backend.service.campus.CampusQueryService;
import com.chordsked.backend.service.campus.CampusUpdateService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1")
@Validated
@Tag(name = "校区管理", description = "校区相关接口")
public class CampusController {
    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Resource(name = "campusQueryService")
    private CampusQueryService campusQueryService;

    @Resource(name = "campusDetailQueryService")
    private CampusDetailQueryService campusDetailQueryService;

    @Resource(name = "campusCreateService")
    private CampusCreateService campusCreateService;

    @Resource(name = "campusUpdateService")
    private CampusUpdateService campusUpdateService;

    @Resource(name = "campusDeleteService")
    private CampusDeleteService campusDeleteService;

    @GetMapping("/campuses")
    @PreAuthorize("hasAuthority('admin:campus:view')")
    @Operation(summary = "分页查询校区列表", description = "按条件分页查询校区列表，并根据当前登录用户的数据权限范围自动过滤结果")
    public ApiResponse<PageResult<CampusQueryResultVO>> listCampuses(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(name = "page", required = false) Integer page,
            @Parameter(description = "每页条数")
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @Parameter(description = "关键词（校区名称/校区编码）")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "校区状态")
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
        if (status != null && CampusStatus.fromCode(status) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        return ApiResponse.success(campusQueryService.list(buildCampusQueryRequest(currentPage, currentPageSize, keyword, status)));
    }

    @PostMapping("/campuses")
    @PreAuthorize("hasAuthority('admin:campus:create')")
    @Operation(summary = "创建校区", description = "创建新校区，设置校区基本信息")
    public ApiResponse<Long> createCampus(
            @Valid @RequestBody CampusCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        Long campusId = campusCreateService.create(buildCampusCreateRequest(request, httpServletRequest));
        return ApiResponse.success(campusId);
    }

    @GetMapping("/campuses/{campusId}")
    @PreAuthorize("hasAuthority('admin:campus:view')")
    @Operation(summary = "查询校区详情", description = "按校区ID查询校区完整详情")
    public ApiResponse<CampusDetailResultVO> getCampusDetail(
            @Parameter(description = "校区ID")
            @PathVariable("campusId") @Min(1) Long campusId
    ) {
        CampusDetailQueryRequest request = new CampusDetailQueryRequest();
        request.setCampusId(campusId);
        return ApiResponse.success(campusDetailQueryService.getDetail(request));
    }

    @PutMapping("/campuses/{campusId}")
    @PreAuthorize("hasAuthority('admin:campus:update')")
    @Operation(summary = "编辑校区", description = "更新校区名称、地址、联系方式、负责人、状态和备注")
    public ApiResponse<Void> updateCampus(
            @Parameter(description = "校区ID")
            @PathVariable("campusId") @Min(1) Long campusId,
            @Valid @RequestBody CampusUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        campusUpdateService.update(buildCampusUpdateRequest(campusId, request, httpServletRequest));
        return ApiResponse.success(null);
    }

    @DeleteMapping("/campuses/{campusId}")
    @PreAuthorize("hasAuthority('admin:campus:delete')")
    @Operation(summary = "删除校区", description = "删除未绑定用户的校区，删除前会检测教务账号和教师关联")
    public ApiResponse<Void> deleteCampus(
            @Parameter(description = "校区ID")
            @PathVariable("campusId") @Min(1) Long campusId,
            HttpServletRequest httpServletRequest
    ) {
        campusDeleteService.delete(buildCampusDeleteRequest(campusId, httpServletRequest));
        return ApiResponse.success(null);
    }

    private CampusQueryRequest buildCampusQueryRequest(Integer page, Integer pageSize, String keyword, Integer status) {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setStatus(status);
        return request;
    }

    private CampusCreateRequest buildCampusCreateRequest(CampusCreateRequest source, HttpServletRequest httpServletRequest) {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode(source.getCode());
        request.setName(source.getName());
        request.setAddress(source.getAddress());
        request.setPhone(source.getPhone());
        request.setLeaderId(source.getLeaderId());
        request.setSort(source.getSort());
        request.setStatus(source.getStatus());
        request.setRemark(source.getRemark());
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }

    private CampusUpdateRequest buildCampusUpdateRequest(Long campusId, CampusUpdateRequest source, HttpServletRequest httpServletRequest) {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(campusId);
        request.setCode(source.getCode());
        request.setName(source.getName());
        request.setAddress(source.getAddress());
        request.setPhone(source.getPhone());
        request.setLeaderId(source.getLeaderId());
        request.setSort(source.getSort());
        request.setStatus(source.getStatus());
        request.setRemark(source.getRemark());
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }

    private CampusDeleteRequest buildCampusDeleteRequest(Long campusId, HttpServletRequest httpServletRequest) {
        CampusDeleteRequest request = new CampusDeleteRequest();
        request.setCampusId(campusId);
        request.setAuditLogRequest(AuditLogRequestUtils.buildAuditLogRequest(httpServletRequest));
        return request;
    }
}
