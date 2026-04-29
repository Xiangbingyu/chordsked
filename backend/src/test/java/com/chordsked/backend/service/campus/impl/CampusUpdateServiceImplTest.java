package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.dto.campus.CampusUpdateRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusDetailQueryService;
import com.chordsked.backend.service.campus.CampusUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_update_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "chordsked.datascope.enabled=false"
})
class CampusUpdateServiceImplTest {
    @Autowired
    private CampusUpdateService campusUpdateService;

    @Autowired
    private CampusDetailQueryService campusDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateCampusSuccessfully() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH-NEW");
        request.setName("西湖新校区");
        request.setAddress("杭州市西湖区新地址");
        request.setPhone("0571-88888888");
        request.setLeaderId(1002L);
        request.setSort(20);
        request.setStatus(1);
        request.setRemark("更新备注");

        campusUpdateService.update(request);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(2L);
        CampusDetailResultVO result = campusDetailQueryService.getDetail(detailRequest);
        assertEquals("CAMPUS-XH-NEW", result.getCode());
        assertEquals("西湖新校区", result.getName());
        assertEquals("杭州市西湖区新地址", result.getAddress());
        assertEquals("0571-88888888", result.getPhone());
        assertEquals(1002L, result.getLeaderId());
        assertEquals("杭州教务一", result.getLeaderName());
        assertEquals(20, result.getSort());
        assertEquals("更新备注", result.getRemark());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status FROM sys_audit_log WHERE biz_id = ? ORDER BY id DESC LIMIT 1",
                2L
        );
        assertEquals("CAMPUS_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("UPDATE_CAMPUS", auditLog.get("action_type"));
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
    }

    @Test
    void shouldSyncLeaderNameWhenLeaderChanged() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH");
        request.setName("西湖校区");
        request.setLeaderId(1003L);
        request.setStatus(1);

        campusUpdateService.update(request);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(2L);
        CampusDetailResultVO result = campusDetailQueryService.getDetail(detailRequest);
        assertEquals(1003L, result.getLeaderId());
        assertEquals("杭州教务二", result.getLeaderName());
    }

    @Test
    void shouldClearLeaderWhenLeaderIdRemoved() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH");
        request.setName("西湖校区");
        request.setLeaderId(null);
        request.setStatus(1);

        campusUpdateService.update(request);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(2L);
        CampusDetailResultVO result = campusDetailQueryService.getDetail(detailRequest);
        assertNull(result.getLeaderId());
    }

    @Test
    void shouldThrowWhenCodeConflictWithOtherCampus() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-BJ");
        request.setName("新名称");
        request.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> campusUpdateService.update(request));
        assertEquals("校区编码已存在，请更换", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNameConflictWithOtherCampus() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH");
        request.setName("滨江校区");
        request.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> campusUpdateService.update(request));
        assertEquals("校区名称已存在，请更换", exception.getMessage());
    }

    @Test
    void shouldThrowWhenCampusNotExists() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(99999L);
        request.setCode("CAMPUS-FAKE");
        request.setName("不存在校区");
        request.setStatus(1);

        assertThrows(BusinessException.class, () -> campusUpdateService.update(request));
    }

    @Test
    void shouldThrowWhenLeaderNotExists() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH");
        request.setName("西湖校区");
        request.setLeaderId(99999L);
        request.setStatus(1);

        assertThrows(BusinessException.class, () -> campusUpdateService.update(request));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> campusUpdateService.update(null));
    }

    @Test
    void shouldThrowWhenInvalidStatus() {
        CampusUpdateRequest request = new CampusUpdateRequest();
        request.setCampusId(2L);
        request.setCode("CAMPUS-XH");
        request.setName("西湖校区");
        request.setStatus(99);

        assertThrows(BusinessException.class, () -> campusUpdateService.update(request));
    }

    private Map<String, Object> waitForAuditLog(String sql, Object... args) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        RuntimeException latestException = null;
        while (System.nanoTime() < deadline) {
            try {
                return jdbcTemplate.queryForMap(sql, args);
            } catch (RuntimeException exception) {
                latestException = exception;
                try {
                    Thread.sleep(20L);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting audit log persistence", interruptedException);
                }
            }
        }
        if (latestException != null) {
            throw latestException;
        }
        throw new IllegalStateException("Audit log not found in expected time window");
    }
}
