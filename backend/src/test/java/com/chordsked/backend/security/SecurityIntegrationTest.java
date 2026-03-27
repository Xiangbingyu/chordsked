package com.chordsked.backend.security;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.vo.StudentVO;
import com.chordsked.backend.service.StudentService;
import com.chordsked.backend.utils.jwt.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @TestConfiguration
    static class SecurityTestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @MockitoBean(name = "studentService")
    private StudentService studentService;

    @Test
    void shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("keyword", "张")
                        .param("level", "初级"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenTypeIsRefresh() throws Exception {
        String refreshToken = jwtTokenUtils.generateToken(1001L, "TEACHER", 3600L, "refresh");
        mockMvc.perform(get("/api/v1/students")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void shouldPassSecurityWhenAccessTokenIsValid() throws Exception {
        when(studentService.listStudents("张", "初级", 1, 2))
                .thenReturn(PageResult.of(1, List.of(new StudentVO(1L, "张小明", 8, "初级"))));

        String accessToken = jwtTokenUtils.generateToken(1001L, "TEACHER", 3600L, "access");
        mockMvc.perform(get("/api/v1/students")
                        .param("page", "1")
                        .param("pageSize", "2")
                        .param("keyword", "张")
                        .param("level", "初级")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("张小明"));
    }
}
