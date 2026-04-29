package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.campus.CampusDeleteRequest;
import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusDeleteService;
import com.chordsked.backend.service.campus.CampusDetailQueryService;
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
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_delete_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql",
        "chordsked.datascope.enabled=false"
})
class CampusDeleteServiceImplTest {
    @Autowired
    private CampusDeleteService campusDeleteService;

    @Autowired
    private CampusDetailQueryService campusDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteCampusWithoutBinding() {
        CampusDeleteRequest request = new CampusDeleteRequest();
        request.setCampusId(5L);

        campusDeleteService.delete(request);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(5L);
        assertThrows(BusinessException.class, () -> campusDetailQueryService.getDetail(detailRequest));

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status FROM sys_audit_log WHERE biz_id = ? ORDER BY id DESC LIMIT 1",
                5L
        );
        assertEquals("CAMPUS_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("DELETE_CAMPUS", auditLog.get("action_type"));
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
    }

    @Test
    void shouldThrowWhenHasInternalUserBinding() {
        CampusDeleteRequest request = new CampusDeleteRequest();
        request.setCampusId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> campusDeleteService.delete(request));
        assertEquals("校区有教务账号绑定，请先移除用户", exception.getMessage());
    }

    @Test
    void shouldThrowWhenHasTeacherBinding() {
        CampusDeleteRequest request = new CampusDeleteRequest();
        request.setCampusId(1L);

        assertThrows(BusinessException.class, () -> campusDeleteService.delete(request));
    }

    @Test
    void shouldThrowWhenCampusNotExists() {
        CampusDeleteRequest request = new CampusDeleteRequest();
        request.setCampusId(99999L);

        assertThrows(BusinessException.class, () -> campusDeleteService.delete(request));
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> campusDeleteService.delete(null));
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
