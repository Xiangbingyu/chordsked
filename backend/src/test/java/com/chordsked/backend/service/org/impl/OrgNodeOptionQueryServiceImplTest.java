package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.model.vo.org.OrgNodeOptionVO;
import com.chordsked.backend.service.org.OrgNodeOptionQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_node_option_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgNodeOptionQueryServiceImplTest {
    @Autowired
    private OrgNodeOptionQueryService orgNodeOptionQueryService;

    @Test
    void shouldListEnabledOrgNodeOptions() {
        List<OrgNodeOptionVO> result = orgNodeOptionQueryService.list();

        assertEquals(5, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(0L, result.get(0).getParentId());
        assertEquals(1, result.get(0).getNodeType());
        assertEquals("默认校区", result.get(0).getName());
        assertEquals(1, result.get(0).getStatus());
    }
}
