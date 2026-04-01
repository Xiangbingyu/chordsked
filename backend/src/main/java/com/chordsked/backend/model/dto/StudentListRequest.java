package com.chordsked.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(name = "StudentListRequest", description = "学员列表查询参数")
public class StudentListRequest {
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    @Min(1)
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20", defaultValue = "20")
    @Min(1)
    private Integer pageSize = 20;

    @Schema(description = "关键词（学员姓名）", example = "张")
    private String keyword;

    @Schema(description = "学员状态", example = "1")
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
}
