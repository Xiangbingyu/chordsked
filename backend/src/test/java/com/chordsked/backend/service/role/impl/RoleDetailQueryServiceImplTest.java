package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.role.RoleDetailQueryRequest;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.service.role.RoleDetailQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_detail_query_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class RoleDetailQueryServiceImplTest {
    @Autowired
    private RoleDetailQueryService roleDetailQueryService;

    @Test
    void shouldReturnRoleDetailWithPermissions() {
        RoleDetailQueryRequest request = new RoleDetailQueryRequest();
        request.setRoleId(1L);

        RoleDetailQueryResultVO result = roleDetailQueryService.getDetail(request);

        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getCode());
        assertEquals("系统管理员", result.getName());
        assertEquals(1, result.getStatus());
        assertEquals(27, result.getPermissionCount());
        assertEquals(1, result.getUserCount());
        assertEquals(27, result.getPermissionIds().size());
        assertEquals(6, result.getPermissionTree().size());
    }

    @Test
    void shouldThrowWhenRoleNotFound() {
        RoleDetailQueryRequest request = new RoleDetailQueryRequest();
        request.setRoleId(999L);

        BusinessException exception = assertThrows(BusinessException.class, () -> roleDetailQueryService.getDetail(request));

        assertEquals(400, exception.getCode());
        assertEquals("角色不存在", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> roleDetailQueryService.getDetail(null));
    }

    @Test
    void shouldThrowWhenRoleIdInvalid() {
        RoleDetailQueryRequest request = new RoleDetailQueryRequest();
        request.setRoleId(0L);

        assertThrows(BusinessException.class, () -> roleDetailQueryService.getDetail(request));
    }
}
