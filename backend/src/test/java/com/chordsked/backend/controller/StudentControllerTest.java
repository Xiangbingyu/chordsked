package com.chordsked.backend.controller;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.cache.SecurityCacheService;
import com.chordsked.backend.model.dto.StudentListRequest;
import com.chordsked.backend.model.enums.StudentUserStatus;
import com.chordsked.backend.model.vo.StudentListResultVO;
import com.chordsked.backend.security.account.service.MultiAccountUserDetailsService;
import com.chordsked.backend.service.StudentListService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "studentListService")
    private StudentListService studentListService;

    @MockitoBean(name = "jwtTokenUtils")
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean(name = "multiAccountUserDetailsService")
    private MultiAccountUserDetailsService multiAccountUserDetailsService;

    @MockitoBean(name = "securityCacheService")
    private SecurityCacheService securityCacheService;

    @Test
    void shouldListStudents() throws Exception {
        when(studentListService.list(any(StudentListRequest.class)))
                .thenReturn(PageResult.of(1, List.of(
                        new StudentListResultVO(1L, "13700000000", "张小明", StudentUserStatus.ENABLED, 1L)
                )));

        mockMvc.perform(get("/students/api/v1/list")
                        .queryParam("page", "1")
                        .queryParam("pageSize", "2")
                        .queryParam("keyword", "张")
                        .queryParam("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("张小明"));
    }
}
