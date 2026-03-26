package com.chordsked.backend.controller;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.vo.StudentVO;
import com.chordsked.backend.service.StudentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "studentService")
    private StudentService studentService;

    @Test
    void shouldListStudents() throws Exception {
        when(studentService.listStudents("张", "初级", 1, 2))
                .thenReturn(PageResult.of(1, List.of(
                        new StudentVO(1L, "张小明", 8, "初级")
                )));

        mockMvc.perform(get("/api/v1/students")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("keyword", "张")
                        .param("level", "初级"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("张小明"));
    }
}
