package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.model.vo.org.OrgTreeNodeVO;
import com.chordsked.backend.service.org.OrgTreeQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_tree_query_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgTreeQueryServiceImplTest {
    @Autowired
    private OrgTreeQueryService orgTreeQueryService;

    @Test
    void shouldListRootCampusNodes() {
        List<OrgTreeNodeVO> result = orgTreeQueryService.listTree();

        assertEquals(5, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("CAMPUS-DEFAULT", result.get(0).getCode());
        assertEquals("默认校区", result.get(0).getName());
        assertEquals(1, result.get(0).getNodeType());
        assertEquals(1, result.get(0).getCampusId());
        assertNotNull(result.get(0).getChildren());
        assertFalse(result.get(0).getHasChildren());
    }

    @Test
    void shouldContainBoundUserCountForCampusNode() {
        List<OrgTreeNodeVO> result = orgTreeQueryService.listTree();

        OrgTreeNodeVO defaultCampus = result.stream()
                .filter(node -> Long.valueOf(1L).equals(node.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, defaultCampus.getBoundUserCount());
        assertTrue(defaultCampus.getChildren().isEmpty());
    }
}
