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

    @Schema(description = "所属校区ID", example = "1")
    private Long campusId;

    @Schema(description = "所属校区名称", example = "默认校区")
    private String campusName;

    @Schema(description = "主归属组织节点ID", example = "1")
    private Long orgNodeId;

    @Schema(description = "主归属组织节点名称", example = "默认校区")
    private String orgNodeName;

    @Schema(description = "主归属组织节点类型", example = "1")
    private Integer orgNodeType;

    @Schema(description = "组织授权节点ID列表")
    private List<Long> orgScopeNodeIds;

    @Schema(description = "绑定角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "绑定角色名称列表")
    private List<String> roleNames;

    @Schema(description = "权限代码列表")
    private List<String> permissionCodes;

    @Schema(description = "是否系统管理员受保护账号", example = "true")
    private Boolean systemAccount;

    @Schema(description = "是否当前登录人本人", example = "false")
    private Boolean currentUser;

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

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public Long getOrgNodeId() {
        return orgNodeId;
    }

    public void setOrgNodeId(Long orgNodeId) {
        this.orgNodeId = orgNodeId;
    }

    public String getOrgNodeName() {
        return orgNodeName;
    }

    public void setOrgNodeName(String orgNodeName) {
        this.orgNodeName = orgNodeName;
    }

    public Integer getOrgNodeType() {
        return orgNodeType;
    }

    public void setOrgNodeType(Integer orgNodeType) {
        this.orgNodeType = orgNodeType;
    }

    public List<Long> getOrgScopeNodeIds() {
        return orgScopeNodeIds;
    }

    public void setOrgScopeNodeIds(List<Long> orgScopeNodeIds) {
        this.orgScopeNodeIds = orgScopeNodeIds;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public Boolean getSystemAccount() {
        return systemAccount;
    }

    public void setSystemAccount(Boolean systemAccount) {
        this.systemAccount = systemAccount;
    }

    public Boolean getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Boolean currentUser) {
        this.currentUser = currentUser;
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
