package com.chordsked.backend.service.role.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.role.RoleCreateRequest;
import com.chordsked.backend.model.dto.role.RoleDeleteRequest;
import com.chordsked.backend.service.role.RoleCreateService;
import com.chordsked.backend.service.role.RoleDeleteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_role_delete_preset_config_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "chordsked.role.preset-role-codes=CONFIG_PRESET_ROLE"
})
class RoleDeleteServicePresetConfigTest {
    @Autowired
    private RoleDeleteService roleDeleteService;

    @Autowired
    private RoleCreateService roleCreateService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldBlockDeletingPresetRoleDefinedByConfig() {
        RoleCreateRequest createRequest = new RoleCreateRequest();
        createRequest.setCode("CONFIG_PRESET_ROLE");
        createRequest.setName("配置预置角色");
        createRequest.setDescription("来自配置项的预置角色");
        createRequest.setStatus(1);
        createRequest.setPermissionIds(List.of(1L, 120L));
        Long roleId = roleCreateService.create(createRequest);

        RoleDeleteRequest deleteRequest = new RoleDeleteRequest();
        deleteRequest.setRoleId(roleId);
        BusinessException exception = assertThrows(BusinessException.class, () -> roleDeleteService.delete(deleteRequest));

        assertEquals(400, exception.getCode());
        assertEquals("系统预置角色不能删除", exception.getMessage());
    }
}
