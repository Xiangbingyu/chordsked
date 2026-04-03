package com.chordsked.backend.model.permission;

import com.chordsked.backend.model.enums.AccountUserType;
import java.util.List;

/**
 * 权限树节点枚举。
 * 用于在代码中维护各账号体系的静态权限树定义，供 PermissionCodeResolver 等鉴权组件读取。
 */
public enum PermissionTreeNode {
    ADMIN_ROOT(1, "账号管理", List.of(), -1, AccountUserType.ADMIN),
    ADMIN_ROLE(2, "教务端入口权限", List.of("admin:role"), 1, AccountUserType.ADMIN),
    TEACHER_ROOT(100, "教师端权限", List.of(), -1, AccountUserType.TEACHER),
    TEACHER_ROLE(101, "教师端入口权限", List.of("teacher:role"), 100, AccountUserType.TEACHER),
    STUDENT_ROOT(200, "学员端权限", List.of(), -1, AccountUserType.STUDENT),
    STUDENT_ROLE(201, "学员端入口权限", List.of("student:role"), 200, AccountUserType.STUDENT);

    private final int code;
    private final String name;
    private final List<String> permissionCodes;
    private final int parentCode;
    private final AccountUserType userType;

    PermissionTreeNode(
            int code,
            String name,
            List<String> permissionCodes,
            int parentCode,
            AccountUserType userType
    ) {
        this.code = code;
        this.name = name;
        this.permissionCodes = permissionCodes;
        this.parentCode = parentCode;
        this.userType = userType;
    }

    /**
     * 返回当前节点编码。
     */
    public int getCode() {
        return code;
    }

    /**
     * 返回当前节点名称。
     */
    public String getName() {
        return name;
    }

    /**
     * 返回当前节点直接挂载的权限码列表。
     */
    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    /**
     * 返回父节点编码；根节点固定为 -1。
     */
    public int getParentCode() {
        return parentCode;
    }

    /**
     * 返回字符串形式的账号类型编码。
     */
    public String getUserType() {
        return userType.getCode();
    }

    /**
     * 返回枚举形式的账号类型。
     */
    public AccountUserType getUserTypeEnum() {
        return userType;
    }

    /**
     * 根据节点编码查找枚举实例；未命中时返回 null。
     */
    public static PermissionTreeNode fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PermissionTreeNode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
