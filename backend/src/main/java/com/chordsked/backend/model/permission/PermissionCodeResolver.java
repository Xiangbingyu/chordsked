package com.chordsked.backend.model.permission;

import com.chordsked.backend.model.enums.AccountUserType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限码解析器。
 * 基于代码内维护的 PermissionTreeNode 定义，提取指定账号体系可用的权限码列表。
 */
public final class PermissionCodeResolver {
    private PermissionCodeResolver() {
    }

    /**
     * 根据字符串形式的 userType 解析权限码。
     * 非法 userType 返回空列表，避免把异常传播到鉴权主流程。
     */
    public static List<String> resolvePermissionCodes(String userType) {
        if (userType == null || userType.isBlank()) {
            return List.of();
        }
        try {
            return resolvePermissionCodes(AccountUserType.fromValue(userType));
        } catch (IllegalArgumentException exception) {
            return List.of();
        }
    }

    /**
     * 根据标准化后的账号类型提取权限码。
     * 当前实现按 userType 过滤节点并汇总 permissionCodes，不直接依赖数据库权限表。
     */
    public static List<String> resolvePermissionCodes(AccountUserType userType) {
        if (userType == null) {
            return List.of();
        }
        return Arrays.stream(PermissionTreeNode.values())
                .filter(node -> node.getUserTypeEnum() == userType)
                .flatMap(node -> node.getPermissionCodes().stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
