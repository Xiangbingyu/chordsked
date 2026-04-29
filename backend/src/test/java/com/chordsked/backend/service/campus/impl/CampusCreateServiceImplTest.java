package com.chordsked.backend.service.campus.impl;

import com.chordsked.backend.exception.BusinessException;
import com.chordsked.backend.model.dto.campus.CampusCreateRequest;
import com.chordsked.backend.model.dto.campus.CampusDetailQueryRequest;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.service.campus.CampusCreateService;
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
        "spring.datasource.url=jdbc:h2:mem:chordsked_campus_create_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:/db/h2/schema.sql",
        "spring.sql.init.data-locations=classpath:/db/data.sql"
})
class CampusCreateServiceImplTest {
    @Autowired
    private CampusCreateService campusCreateService;

    @Autowired
    private CampusDetailQueryService campusDetailQueryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldCreateCampusSuccessfully() {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode("CAMPUS-TEST");
        request.setName("测试校区");
        request.setAddress("测试地址");
        request.setPhone("010-12345678");
        request.setLeaderId(1001L);
        request.setSort(10);
        request.setStatus(1);
        request.setRemark("测试备注");

        Long campusId = campusCreateService.create(request);

        assertNotNull(campusId);
        assertTrue(campusId > 0);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(campusId);
        CampusDetailResultVO result = campusDetailQueryService.getDetail(detailRequest);
        assertEquals("CAMPUS-TEST", result.getCode());
        assertEquals("测试校区", result.getName());
        assertEquals("测试地址", result.getAddress());
        assertEquals("010-12345678", result.getPhone());
        assertEquals(1001L, result.getLeaderId());
        assertEquals("系统管理员", result.getLeaderName());
        assertEquals(10, result.getSort());
        assertEquals(1, result.getStatus());
        assertEquals("测试备注", result.getRemark());

        Map<String, Object> auditLog = waitForAuditLog(
                "SELECT module_name, action_type, biz_id, status FROM sys_audit_log WHERE biz_id = ? ORDER BY id DESC LIMIT 1",
                campusId
        );
        assertEquals("CAMPUS_MANAGEMENT", auditLog.get("module_name"));
        assertEquals("CREATE_CAMPUS", auditLog.get("action_type"));
        assertEquals(campusId.longValue(), ((Number) auditLog.get("biz_id")).longValue());
        assertEquals(1, ((Number) auditLog.get("status")).intValue());
    }

    @Test
    void shouldThrowWhenCodeAlreadyExists() {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode("CAMPUS-DEFAULT");
        request.setName("重复编码校区");
        request.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> campusCreateService.create(request));
        assertEquals(400, exception.getCode());
        assertEquals("校区编码已存在，请更换", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNameAlreadyExists() {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode("CAMPUS-UNIQUE-NEW");
        request.setName("默认校区");
        request.setStatus(1);

        BusinessException exception = assertThrows(BusinessException.class, () -> campusCreateService.create(request));
        assertEquals(400, exception.getCode());
        assertEquals("校区名称已存在，请更换", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRequestIsNull() {
        assertThrows(BusinessException.class, () -> campusCreateService.create(null));
    }

    @Test
    void shouldThrowWhenCodeBlank() {
        CampusCreateRequest request = buildValidRequest();
        request.setCode("   ");

        assertThrows(BusinessException.class, () -> campusCreateService.create(request));
    }

    @Test
    void shouldThrowWhenNameBlank() {
        CampusCreateRequest request = buildValidRequest();
        request.setName("");

        assertThrows(BusinessException.class, () -> campusCreateService.create(request));
    }

    @Test
    void shouldThrowWhenStatusInvalid() {
        CampusCreateRequest request = buildValidRequest();
        request.setStatus(99);

        assertThrows(BusinessException.class, () -> campusCreateService.create(request));
    }

    @Test
    void shouldThrowWhenLeaderNotExists() {
        CampusCreateRequest request = buildValidRequest();
        request.setCode("CAMPUS-NO-LEADER");
        request.setLeaderId(99999L);

        assertThrows(BusinessException.class, () -> campusCreateService.create(request));
    }

    @Test
    void shouldCreateCampusWithoutLeader() {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode("CAMPUS-NO-LEADER-OK");
        request.setName("无负责人校区");
        request.setStatus(1);
        request.setLeaderId(null);

        Long campusId = campusCreateService.create(request);
        assertNotNull(campusId);

        CampusDetailQueryRequest detailRequest = new CampusDetailQueryRequest();
        detailRequest.setCampusId(campusId);
        CampusDetailResultVO result = campusDetailQueryService.getDetail(detailRequest);
        assertNull(result.getLeaderId());
        assertNull(result.getLeaderName());
    }

    private CampusCreateRequest buildValidRequest() {
        CampusCreateRequest request = new CampusCreateRequest();
        request.setCode("CAMPUS-VALID-TEST");
        request.setName("有效测试校区");
        request.setStatus(1);
        return request;
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
