package com.chordsked.backend.model.vo.internaluser;

import com.chordsked.backend.model.enums.InternalUserStatus;
import com.chordsked.backend.model.enums.MustChangePasswordFlag;
import com.chordsked.backend.model.enums.UserDataScopeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "InternalUserDetailResultVO", description = "教务端账号详情查询结果")
public class InternalUserDetailResultVO {
    @Schema(description = "用户ID", example = "1001")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "姓名", example = "系统管理员")
    private String name;

    @Schema(description = "头像", example = "https://example.com/avatar.png")
    private String avatar;

    @Schema(description = "账号状态", example = "1")
    private InternalUserStatus status;

    @Schema(description = "是否强制改密", example = "1")
    private MustChangePasswordFlag mustChangePassword;

    @Schema(description = "数据权限类型", example = "1")
    private UserDataScopeType dataScopeType;

    @Schema(description = "主校区ID", example = "1")
    private Long primaryCampusId;

    @Schema(description = "绑定校区ID列表")
    private List<Long> campusIds;

    @Schema(description = "绑定角色ID列表")
    private List<Long> roleIds;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getStatus() {
        return status == null ? null : status.getCode();
    }

    public void setStatus(Integer status) {
        this.status = InternalUserStatus.fromCode(status);
    }

    public InternalUserStatus getStatusEnum() {
        return status;
    }

    public void setStatusEnum(InternalUserStatus status) {
        this.status = status;
    }

    public Integer getMustChangePassword() {
        return mustChangePassword == null ? null : mustChangePassword.getCode();
    }

    public void setMustChangePassword(Integer mustChangePassword) {
        this.mustChangePassword = MustChangePasswordFlag.fromCode(mustChangePassword);
    }

    public MustChangePasswordFlag getMustChangePasswordEnum() {
        return mustChangePassword;
    }

    public void setMustChangePasswordEnum(MustChangePasswordFlag mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Integer getDataScopeType() {
        return dataScopeType == null ? null : dataScopeType.getCode();
    }

    public void setDataScopeType(Integer dataScopeType) {
        this.dataScopeType = UserDataScopeType.fromCode(dataScopeType);
    }

    public UserDataScopeType getDataScopeTypeEnum() {
        return dataScopeType;
    }

    public void setDataScopeTypeEnum(UserDataScopeType dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public Long getPrimaryCampusId() {
        return primaryCampusId;
    }

    public void setPrimaryCampusId(Long primaryCampusId) {
        this.primaryCampusId = primaryCampusId;
    }

    public List<Long> getCampusIds() {
        return campusIds;
    }

    public void setCampusIds(List<Long> campusIds) {
        this.campusIds = campusIds;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
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
