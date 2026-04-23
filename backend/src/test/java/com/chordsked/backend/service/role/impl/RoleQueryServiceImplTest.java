package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import com.chordsked.backend.service.role.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_query_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleQueryServiceImplTest {
    @Autowired
    private RoleQueryService roleQueryService;

    @Test
    void shouldReturnPagedRoleList() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setPageSize(20);

        PageResult<RoleQueryResultVO> result = roleQueryService.list(request);

        assertEquals(2L, result.getTotal());
        assertEquals(2, result.getItems().size());
        assertEquals(List.of("ADMIN", "OPERATOR"), result.getItems().stream().map(RoleQueryResultVO::getCode).toList());
        assertEquals(27, result.getItems().get(0).getPermissionCount());
        assertEquals(1, result.getItems().get(0).getUserCount());
        assertEquals(12, result.getItems().get(1).getPermissionCount());
        assertEquals(0, result.getItems().get(1).getUserCount());
    }

    @Test
    void shouldFilterByKeywordAndStatus() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("OPER");
        request.setStatus(1);

        PageResult<RoleQueryResultVO> result = roleQueryService.list(request);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("OPERATOR", result.getItems().get(0).getCode());
    }

    @Test
    void shouldTrimKeywordBeforeQuery() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("  OPER  ");
        request.setStatus(1);

        PageResult<RoleQueryResultVO> result = roleQueryService.list(request);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("OPERATOR", result.getItems().get(0).getCode());
    }

    @Test
    void shouldThrowWhenPageInvalid() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(0);
        request.setPageSize(20);

        assertThrows(IllegalArgumentException.class, () -> roleQueryService.list(request));
    }

    @Test
    void shouldThrowWhenPageSizeTooLarge() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setPageSize(101);

        assertThrows(IllegalArgumentException.class, () -> roleQueryService.list(request));
    }

    @Test
    void shouldThrowWhenStatusInvalid() {
        RoleQueryRequest request = new RoleQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setStatus(999);

        assertThrows(IllegalArgumentException.class, () -> roleQueryService.list(request));
    }

}
