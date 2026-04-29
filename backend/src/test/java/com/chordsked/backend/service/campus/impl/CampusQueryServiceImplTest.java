package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;
import com.chordsked.backend.service.campus.CampusQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_query_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "chordsked.datascope.enabled=false"
})
class CampusQueryServiceImplTest {
    @Autowired
    private CampusQueryService campusQueryService;

    @Test
    void shouldListAllCampusesWithDefaultPage() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(20);

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(5, result.getTotal());
        assertEquals(5, result.getItems().size());
        assertEquals("CAMPUS-DEFAULT", result.getItems().get(0).getCode());
    }

    @Test
    void shouldFilterByKeyword() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("西湖");

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(1, result.getTotal());
        assertEquals("西湖校区", result.getItems().get(0).getName());
    }

    @Test
    void shouldFilterByKeywordWithCode() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("CAMPUS-XH");

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(1, result.getTotal());
        assertEquals("CAMPUS-XH", result.getItems().get(0).getCode());
    }

    @Test
    void shouldFilterByStatus() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setStatus(1);

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(5, result.getTotal());
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(20);
        request.setKeyword("不存在的校区");

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void shouldPaginateCorrectly() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(2);

        PageResult<CampusQueryResultVO> result = campusQueryService.list(request);

        assertEquals(5, result.getTotal());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> campusQueryService.list(null));
    }

    @Test
    void shouldThrowWhenPageLessThanOne() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(0);
        request.setPageSize(10);

        assertThrows(BusinessException.class, () -> campusQueryService.list(request));
    }

    @Test
    void shouldThrowWhenPageSizeLessThanOne() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(0);

        assertThrows(BusinessException.class, () -> campusQueryService.list(request));
    }

    @Test
    void shouldThrowWhenInvalidStatus() {
        CampusQueryRequest request = new CampusQueryRequest();
        request.setPage(1);
        request.setPageSize(10);
        request.setStatus(99);

        assertThrows(BusinessException.class, () -> campusQueryService.list(request));
    }
}
