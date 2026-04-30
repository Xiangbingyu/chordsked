package com.chordsked.backend.model.vo.org;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "OrgTreeNodeVO", description = "组织树节点")
public class OrgTreeNodeVO {
    @Schema(description = "组织节点ID", example = "1")
    private Long id;

    @Schema(description = "父节点ID", example = "0")
    private Long parentId;

    @Schema(description = "节点类型", example = "1")
    private Integer nodeType;

    @Schema(description = "节点编码", example = "CAMPUS-DEFAULT")
    private String code;

    @Schema(description = "节点名称", example = "默认校区")
    private String name;

    @Schema(description = "所属校区ID", example = "1")
    private Long campusId;

    @Schema(description = "层级深度", example = "1")
    private Integer level;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "初始化根节点")
    private String remark;

    @Schema(description = "绑定账号数量", example = "2")
    private Integer boundUserCount;

    @Schema(description = "是否有子节点", example = "true")
    private Boolean hasChildren;

    @Schema(description = "子节点列表")
    private List<OrgTreeNodeVO> children;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getBoundUserCount() {
        return boundUserCount;
    }

    public void setBoundUserCount(Integer boundUserCount) {
        this.boundUserCount = boundUserCount;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public List<OrgTreeNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<OrgTreeNodeVO> children) {
        this.children = children;
    }
}
