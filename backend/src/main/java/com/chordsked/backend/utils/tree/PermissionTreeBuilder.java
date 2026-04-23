package com.chordsked.backend.utils.tree;

import com.chordsked.backend.model.entity.PermissionEntity;
import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component("permissionTreeBuilder")
public class PermissionTreeBuilder {
    public List<InternalPermissionTreeQueryResultVO> build(List<PermissionEntity> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        Map<Long, InternalPermissionTreeQueryResultVO> nodeMap = new LinkedHashMap<>();
        for (PermissionEntity permission : permissions) {
            if (permission == null || permission.getId() == null) {
                continue;
            }
            nodeMap.put(permission.getId(), toNode(permission));
        }
        List<InternalPermissionTreeQueryResultVO> roots = new ArrayList<>();
        for (PermissionEntity permission : permissions) {
            if (permission == null || permission.getId() == null) {
                continue;
            }
            InternalPermissionTreeQueryResultVO currentNode = nodeMap.get(permission.getId());
            Long parentId = permission.getParentId();
            if (parentId != null && nodeMap.containsKey(parentId)) {
                nodeMap.get(parentId).getChildren().add(currentNode);
                continue;
            }
            roots.add(currentNode);
        }
        sortNodes(roots);
        return roots;
    }

    private InternalPermissionTreeQueryResultVO toNode(PermissionEntity permission) {
        return new InternalPermissionTreeQueryResultVO(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getType(),
                permission.getPath(),
                permission.getSort()
        );
    }

    private void sortNodes(List<InternalPermissionTreeQueryResultVO> nodes) {
        nodes.sort(Comparator
                .comparing(InternalPermissionTreeQueryResultVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(InternalPermissionTreeQueryResultVO::getId, Comparator.nullsLast(Long::compareTo)));
        for (InternalPermissionTreeQueryResultVO node : nodes) {
            if (node == null || Objects.requireNonNullElse(node.getChildren(), List.<InternalPermissionTreeQueryResultVO>of()).isEmpty()) {
                continue;
            }
            sortNodes(node.getChildren());
        }
    }
}
