package com.chordsked.backend.service.permission.impl;

import com.chordsked.backend.model.vo.permission.InternalPermissionTreeQueryResultVO;
import com.chordsked.backend.service.permission.InternalPermissionTreeQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_permission_tree_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class InternalPermissionTreeQueryServiceImplTest {
    @Autowired
    private InternalPermissionTreeQueryService internalPermissionTreeQueryService;

    @Test
    void shouldReturnFullAdminPermissionTreeFromSeedData() {
        List<InternalPermissionTreeQueryResultVO> result = internalPermissionTreeQueryService.listAdminPermissionTree();

        assertEquals(List.of(
                "admin:role",
                "admin:auth:menu",
                "admin:user:menu",
                "admin:role:menu",
                "admin:org:menu",
                "admin:log:menu"
        ), result.stream().map(InternalPermissionTreeQueryResultVO::getCode).toList());

        Map<String, InternalPermissionTreeQueryResultVO> rootNodeMap = toNodeMap(result);
        assertEquals(List.of(), rootNodeMap.get("admin:role").getChildren());
        assertEquals(List.of("admin:auth:logout"), childCodes(rootNodeMap.get("admin:auth:menu")));
        assertEquals(List.of(
                "admin:user:view",
                "admin:user:create",
                "admin:user:update",
                "admin:user:delete",
                "admin:user:enable",
                "admin:user:reset_password"
        ), childCodes(rootNodeMap.get("admin:user:menu")));
        assertEquals(List.of(
                "admin:role:view",
                "admin:role:create",
                "admin:role:update",
                "admin:role:delete",
                "admin:role:assign_permission"
        ), childCodes(rootNodeMap.get("admin:role:menu")));
        assertEquals(List.of(
                "admin:org:view",
                "admin:org:create",
                "admin:org:update",
                "admin:org:delete",
                "admin:org:assign_user"
        ), childCodes(rootNodeMap.get("admin:org:menu")));
        assertEquals(List.of(
                "admin:log:view",
                "admin:log:detail",
                "admin:log:export",
                "admin:log:sensitive"
        ), childCodes(rootNodeMap.get("admin:log:menu")));
        assertEquals(27, flattenCodes(result).size());
        assertFalse(flattenCodes(result).contains("teacher:role"));
        assertFalse(flattenCodes(result).contains("student:role"));
    }

    private Map<String, InternalPermissionTreeQueryResultVO> toNodeMap(List<InternalPermissionTreeQueryResultVO> nodes) {
        Map<String, InternalPermissionTreeQueryResultVO> nodeMap = new LinkedHashMap<>();
        for (InternalPermissionTreeQueryResultVO node : nodes) {
            nodeMap.put(node.getCode(), node);
        }
        return nodeMap;
    }

    private List<String> childCodes(InternalPermissionTreeQueryResultVO node) {
        return node.getChildren().stream()
                .map(InternalPermissionTreeQueryResultVO::getCode)
                .toList();
    }

    private List<String> flattenCodes(List<InternalPermissionTreeQueryResultVO> nodes) {
        List<String> codes = new ArrayList<>();
        for (InternalPermissionTreeQueryResultVO node : nodes) {
            codes.add(node.getCode());
            codes.addAll(flattenCodes(node.getChildren()));
        }
        return codes;
    }
}
