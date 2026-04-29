package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusDetailQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_detail_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class CampusDetailQueryServiceImplTest {
    @Autowired
    private CampusDetailQueryService campusDetailQueryService;

    @Test
    void shouldGetCampusDetail() {
        CampusDetailQueryRequest request = new CampusDetailQueryRequest();
        request.setCampusId(2L);

        CampusDetailResultVO result = campusDetailQueryService.getDetail(request);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("CAMPUS-XH", result.getCode());
        assertEquals("西湖校区", result.getName());
        assertEquals("杭州西湖区", result.getAddress());
        assertEquals("0571-00000001", result.getPhone());
        assertEquals(1001L, result.getLeaderId());
        assertEquals("系统管理员", result.getLeaderName());
        assertEquals(2, result.getSort());
        assertEquals(1, result.getStatus());
        assertEquals("联调用测试校区", result.getRemark());
    }

    @Test
    void shouldThrowWhenCampusNotExists() {
        CampusDetailQueryRequest request = new CampusDetailQueryRequest();
        request.setCampusId(99999L);

        assertThrows(BusinessException.class, () -> campusDetailQueryService.getDetail(request));
    }

    @Test
    void shouldThrowWhenCampusIdInvalid() {
        CampusDetailQueryRequest request = new CampusDetailQueryRequest();
        request.setCampusId(0L);

        assertThrows(IllegalArgumentException.class, () -> campusDetailQueryService.getDetail(request));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(IllegalArgumentException.class, () -> campusDetailQueryService.getDetail(null));
    }
}
