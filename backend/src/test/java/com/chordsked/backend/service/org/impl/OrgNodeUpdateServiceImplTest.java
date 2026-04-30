package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.model.dto.org.OrgNodeUpdateRequest;
import com.chordsked.backend.service.org.OrgNodeUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_node_update_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgNodeUpdateServiceImplTest {
    @Autowired
    private OrgNodeUpdateService orgNodeUpdateService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateCampusRootNodeAndSyncCampusSuccessfully() {
        OrgNodeUpdateRequest request = new OrgNodeUpdateRequest();
        request.setNodeId(2L);
        request.setCode("CAMPUS-XH-NEW");
        request.setName("西湖校区新版");
        request.setSort(20);
        request.setStatus(0);
        request.setRemark("组织管理修改校区");

        orgNodeUpdateService.update(request);

        Map<String, Object> orgNode = jdbcTemplate.queryForMap(
                "SELECT code, name, sort, status, remark FROM sys_org_node WHERE id = 2"
        );
        assertEquals("CAMPUS-XH-NEW", orgNode.get("code"));
        assertEquals("西湖校区新版", orgNode.get("name"));
        assertEquals(20, ((Number) orgNode.get("sort")).intValue());
        assertEquals(0, ((Number) orgNode.get("status")).intValue());
        assertEquals("组织管理修改校区", orgNode.get("remark"));

        Map<String, Object> campus = jdbcTemplate.queryForMap(
                "SELECT code, name, sort, status, remark FROM sys_campus WHERE id = 2"
        );
        assertEquals("CAMPUS-XH-NEW", campus.get("code"));
        assertEquals("西湖校区新版", campus.get("name"));
        assertEquals(20, ((Number) campus.get("sort")).intValue());
        assertEquals(0, ((Number) campus.get("status")).intValue());
        assertEquals("组织管理修改校区", campus.get("remark"));
    }
}
