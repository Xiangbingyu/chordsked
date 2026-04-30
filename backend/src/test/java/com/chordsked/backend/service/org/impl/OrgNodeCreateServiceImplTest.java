package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.org.OrgNodeCreateRequest;
import com.chordsked.backend.service.org.OrgNodeCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_node_create_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgNodeCreateServiceImplTest {
    @Autowired
    private OrgNodeCreateService orgNodeCreateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateCampusRootNodeSuccessfully() {
        OrgNodeCreateRequest request = new OrgNodeCreateRequest();
        request.setParentId(0L);
        request.setNodeType(1);
        request.setCode("CAMPUS-QS");
        request.setName("钱塘校区");
        request.setSort(6);
        request.setStatus(1);
        request.setRemark("组织管理创建根节点");

        Long nodeId = orgNodeCreateService.create(request);

        assertNotNull(nodeId);

        Map<String, Object> orgNode = jdbcTemplate.queryForMap(
                "SELECT parent_id, node_type, code, name, campus_id, level, sort, status, remark FROM sys_org_node WHERE id = ?",
                nodeId
        );
        assertEquals(0L, ((Number) orgNode.get("parent_id")).longValue());
        assertEquals(1, ((Number) orgNode.get("node_type")).intValue());
        assertEquals("CAMPUS-QS", orgNode.get("code"));
        assertEquals("钱塘校区", orgNode.get("name"));
        assertEquals(1, ((Number) orgNode.get("level")).intValue());
        assertEquals(6, ((Number) orgNode.get("sort")).intValue());

        Long campusId = ((Number) orgNode.get("campus_id")).longValue();
        Map<String, Object> campus = jdbcTemplate.queryForMap(
                "SELECT code, name, sort, status, remark FROM sys_campus WHERE id = ?",
                campusId
        );
        assertEquals("CAMPUS-QS", campus.get("code"));
        assertEquals("钱塘校区", campus.get("name"));
        assertEquals(6, ((Number) campus.get("sort")).intValue());
        assertEquals(1, ((Number) campus.get("status")).intValue());
        assertEquals("组织管理创建根节点", campus.get("remark"));
    }

    @Test
    void shouldThrowWhenCampusRootParentIsNotZero() {
        OrgNodeCreateRequest request = new OrgNodeCreateRequest();
        request.setParentId(1L);
        request.setNodeType(1);
        request.setCode("CAMPUS-INVALID");
        request.setName("非法校区");
        request.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> orgNodeCreateService.create(request));
        assertEquals("CAMPUS 节点父级必须是根节点", exception.getMessage());
    }
}
