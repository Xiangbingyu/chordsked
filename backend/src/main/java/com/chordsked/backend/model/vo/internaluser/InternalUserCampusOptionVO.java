package com.chordsked.backend.model.vo.internaluser;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalUserCampusOptionVO", description = "教务端账号编辑可选校区")
public class InternalUserCampusOptionVO {
    @Schema(description = "校区ID", example = "1")
    private Long id;

    @Schema(description = "校区名称", example = "默认校区")
    private String name;

    @Schema(description = "校区状态，0=禁用，1=启用，2=已删除", example = "1")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
