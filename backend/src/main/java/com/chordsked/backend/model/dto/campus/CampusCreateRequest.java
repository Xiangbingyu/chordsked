package com.chordsked.backend.model.dto.campus;

import com.chordsked.backend.model.dto.audit.AuditLogRecordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CampusCreateRequest", description = "创建校区请求")
public class CampusCreateRequest {
    @Schema(description = "校区编码", example = "CAMPUS-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    @Schema(description = "校区名称", example = "北京校区", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String name;

    @Schema(description = "校区地址", example = "北京市朝阳区xxx路xxx号")
    private String address;

    @Schema(description = "联系电话", example = "010-12345678")
    private String phone;

    @Schema(description = "排序号", example = "1")
    private Integer sort;

    @Schema(description = "状态（0:停用 1:启用）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "备注", example = "新校区")
    private String remark;

    private AuditLogRecordRequest auditLogRequest;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
