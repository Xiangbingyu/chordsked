package com.chordsked.backend.model.entity;

import com.chordsked.backend.model.enums.OrgNodeStatus;
import com.chordsked.backend.model.enums.OrgNodeType;

public class OrgNodeEntity {
    private Long id;
    private Long parentId;
    private OrgNodeType nodeType;
    private String code;
    private String name;
    private Long campusId;
    private String ancestors;
    private Integer level;
    private Integer sort;
    private OrgNodeStatus status;
    private String remark;
    private Long createdAt;
    private Long updatedAt;

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
        return nodeType == null ? null : nodeType.getCode();
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = OrgNodeType.fromCode(nodeType);
    }

    public OrgNodeType getNodeTypeEnum() {
        return nodeType;
    }

    public void setNodeTypeEnum(OrgNodeType nodeType) {
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

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
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
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = OrgNodeStatus.fromCode(status);
    }

    public OrgNodeStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(OrgNodeStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
