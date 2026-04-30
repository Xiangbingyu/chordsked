package com.chordsked.backend.model.vo.org;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrgAccountOptionVO", description = "可绑定组织账号选项")
public class OrgAccountOptionVO {
    @Schema(description = "账号ID", example = "1002")
    private Long userId;

    @Schema(description = "用户名", example = "operator_hz_01")
    private String username;

    @Schema(description = "姓名", example = "杭州教务一")
    private String name;

    @Schema(description = "数据权限类型", example = "2")
    private Integer dataScopeType;

    @Schema(description = "所属校区ID", example = "1")
    private Long campusId;

    @Schema(description = "主归属组织节点ID", example = "1")
    private Long orgNodeId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(Integer dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public Long getOrgNodeId() {
        return orgNodeId;
    }

    public void setOrgNodeId(Long orgNodeId) {
        this.orgNodeId = orgNodeId;
    }
}
