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
                UserDataScopeType.ALL_COMPANY,
                List.of()
        );

        String condition = new AllDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals("", condition);
    }

    @Test
    void shouldBuildCampusConditionForCampusScope() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.SPECIFIED_CAMPUS,
                List.of(1L, 2L)
        );

        String condition = new CampusDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals(
                "EXISTS (SELECT 1 FROM sys_user_campus ds_uc WHERE ds_uc.user_id = u.id AND ds_uc.campus_id IN (1, 2))",
                condition
        );
    }

    @Test
    void shouldBuildFalseConditionWhenCampusScopeHasNoCampusIds() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.SPECIFIED_CAMPUS,
                List.of()
        );

        String condition = new CampusDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals("1 = 0", condition);
    }

    @Test
    void shouldBuildSelfConditionForSelfScope() {
        DataScopeUserContext userContext = new DataScopeUserContext(
                1001L,
                AccountUserType.ADMIN,
                UserDataScopeType.SELF_ONLY,
                List.of()
        );

        String condition = new SelfDataScopeStrategy().buildCondition(userContext, "u", "id");

        assertEquals("u.id = 1001", condition);
    }
}
