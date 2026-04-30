package com.chordsked.backend.model.dto.org;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "OrgNodeUpdateRequest", description = "更新组织节点请求参数")
public class OrgNodeUpdateRequest {
    @Schema(hidden = true)
    @Min(1)
    private Long nodeId;

    @Schema(description = "节点编码", example = "DEPT-JW")
    @NotBlank
    @Size(max = 50)
    private String code;

    @Schema(description = "节点名称", example = "教务部")
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "状态", example = "1")
    @NotNull
    private Integer status;

    @Schema(description = "备注", example = "一期更新")
    @Size(max = 200)
    private String remark;

    @Schema(hidden = true)
    private AuditLogRecordRequest auditLogRequest;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
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

    public AuditLogRecordRequest getAuditLogRequest() {
        return auditLogRequest;
    }

    public void setAuditLogRequest(AuditLogRecordRequest auditLogRequest) {
        this.auditLogRequest = auditLogRequest;
    }
}
