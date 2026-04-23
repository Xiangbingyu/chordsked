package com.chordsked.backend.model.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "RoleQueryRequest", description = "角色分页查询参数")
public class RoleQueryRequest {
    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(1)
    private Integer page;

    @Schema(description = "每页条数", example = "20")
    @Min(1)
    private Integer pageSize;

    @Schema(description = "关键词（角色名称/角色编码）", example = "管理员")
    private String keyword;

    @Schema(description = "角色状态", example = "1")
    private Integer status;

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

    public Integer getOffset() {
        int currentPage = page == null || page < 1 ? 1 : page;
        int currentPageSize = pageSize == null || pageSize < 1 ? 0 : pageSize;
        return (currentPage - 1) * currentPageSize;
    }
}
