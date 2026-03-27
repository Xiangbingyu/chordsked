package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.vo.StudentVO;
import com.chordsked.backend.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/admin/students")
@Validated
@Tag(name = "学员管理", description = "学员相关接口")
public class StudentController {

    @Resource(name = "studentService")
    private StudentService studentService;

    @GetMapping
    @Operation(summary = "查询学员列表", description = "按条件分页查询学员列表")
    public ApiResponse<PageResult<StudentVO>> listStudents(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @Parameter(description = "每页条数")
            @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) int pageSize,
            @Parameter(description = "关键词（学员姓名）")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "等级（例如：初级/中级/体验）")
            @RequestParam(name = "level", required = false) String level
    ) {
        return ApiResponse.success(studentService.listStudents(keyword, level, page, pageSize));
    }
}
