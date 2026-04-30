package com.chordsked.backend.model.vo.campus;

import com.chordsked.backend.model.enums.CampusStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CampusQueryResultVO", description = "校区查询结果")
public class CampusQueryResultVO {
    @Schema(description = "校区ID", example = "1")
    private Long id;

    @Schema(description = "校区编码", example = "CAMPUS-XH")
    private String code;

    @Schema(description = "校区名称", example = "西湖校区")
    private String name;

    @Schema(description = "联系电话", example = "0571-00000001")
    private String phone;

    @Schema(description = "校区状态", example = "1")
    private CampusStatus status;

    @Schema(description = "更新时间", example = "1774483200000")
    private Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = CampusStatus.fromCode(status);
    }

    public CampusStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(CampusStatus status) {
        this.status = status;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
