package com.chordsked.backend.datascope.strategy;

import com.chordsked.backend.datascope.context.DataScopeUserContext;
import com.chordsked.backend.model.enums.AccountUserType;
import com.chordsked.backend.model.enums.UserDataScopeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataScopeStrategyTest {
    @Test
    void shouldReturnEmptyConditionForAllScope() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.ALL,
                null,
                List.of()
        );

        String condition = new AllDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals("", condition);
    }

    @Test
    void shouldReturnDenyAllConditionForUnsupportedField() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.ASSIGNED,
                11L,
                List.of(11L, 22L)
        );

        String condition = new AssignedDataScopeStrategy().buildCondition(userContext, "u", "campus_id");

        assertEquals("1 = 0", condition);
    }

    @Test
    void shouldBuildAssignedUserConditionForIdField() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.ASSIGNED,
                11L,
                List.of(11L, 22L)
        );

        String condition = new AssignedDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals(
                "EXISTS (SELECT 1 FROM sys_user_org_scope ds_uos WHERE ds_uos.user_id = u.id AND ds_uos.org_node_id IN (11, 22))",
                condition
        );
    }

    @Test
    void shouldBuildSelfConditionForSelfScope() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.SELF,
                null,
                List.of()
        );

        String condition = new SelfDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals("u.id = 1001", condition);
    }
}
