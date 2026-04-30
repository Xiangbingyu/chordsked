package com.chordsked.backend.model.vo.campus;

import com.chordsked.backend.model.enums.CampusStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CampusDetailResultVO", description = "校区详情结果")
public class CampusDetailResultVO {
    @Schema(description = "校区ID", example = "1")
    private Long id;

    @Schema(description = "校区编码", example = "CAMPUS-XH")
    private String code;

    @Schema(description = "校区名称", example = "西湖校区")
    private String name;

    @Schema(description = "校区地址", example = "杭州西湖区")
    private String address;

    @Schema(description = "联系电话", example = "0571-00000001")
    private String phone;

    @Schema(description = "排序号", example = "2")
    private Integer sort;

    @Schema(description = "校区状态", example = "1")
    private CampusStatus status;

    @Schema(description = "备注", example = "联调用测试校区")
    private String remark;

    @Schema(description = "创建时间", example = "1774483200000")
    private Long createdAt;

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
