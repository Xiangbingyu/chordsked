package com.chordsked.backend.model.vo.org;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrgNodeOptionVO", description = "组织节点选项")
public class OrgNodeOptionVO {
    @Schema(description = "组织节点ID", example = "1")
    private Long id;

    @Schema(description = "父节点ID", example = "0")
    private Long parentId;

    @Schema(description = "节点类型", example = "1")
    private Integer nodeType;

    @Schema(description = "节点名称", example = "默认校区")
    private String name;

    @Schema(description = "节点状态", example = "1")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
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
