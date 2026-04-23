package com.chordsked.backend.model.dto.internaluser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "InternalUserQueryRequest", description = "教务端账号查询参数")
public class InternalUserQueryRequest {
    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(1)
    private Integer page;

    @Schema(description = "每页条数", example = "20")
    @Min(1)
    private Integer pageSize;

    @Schema(description = "关键词（用户名/姓名/手机号）", example = "admin")
    private String keyword;

    @Schema(description = "账号状态", example = "1")
    private Integer status;

    @Schema(description = "角色ID", example = "2")
    private Long roleId;

    @Schema(description = "校区ID", example = "1")
    private Long campusId;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public Integer getOffset() {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 0 : pageSize;
        return (currentPage - 1) * currentPageSize;
    }
}
