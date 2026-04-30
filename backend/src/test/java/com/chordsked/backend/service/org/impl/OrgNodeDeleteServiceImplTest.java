package com.chordsked.backend.service.org.impl;

import com.chordsked.backend.service.org.OrgNodeDeleteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_org_node_delete_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class OrgNodeDeleteServiceImplTest {
    @Autowired
    private OrgNodeDeleteService orgNodeDeleteService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteCampusRootNodeAndCampusSuccessfully() {
        jdbcTemplate.update(
                "DELETE FROM sys_user_campus WHERE campus_id = ?",
                5L
        );

        orgNodeDeleteService.delete(5L);

        Integer orgNodeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_org_node WHERE id = 5",
                Integer.class
        );
        Integer campusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_campus WHERE id = 5",
                Integer.class
        );

        assertEquals(0, orgNodeCount);
        assertEquals(0, campusCount);
    }
}
