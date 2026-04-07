package com.chordsked.backend.controller;

import com.chordsked.backend.common.ApiResponse;
import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.student.StudentListRequest;
import com.chordsked.backend.model.enums.StudentUserStatus;
import com.chordsked.backend.model.vo.student.StudentListResultVO;
import com.chordsked.backend.service.student.StudentListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students/api/v1")
@Validated
@Tag(name = "学员管理", description = "学员相关接口")
public class StudentController {

    @Resource(name = "studentListService")
    private StudentListService studentListService;

    @GetMapping("/list")
    @Operation(summary = "查询学员列表", description = "按条件分页查询学员列表")
    public ApiResponse<PageResult<StudentListResultVO>> list(
            @Parameter(description = "页码，从 1 开始")
            @RequestParam(name = "page", defaultValue = "1") @Min(1) Integer page,
            @Parameter(description = "每页条数")
            @RequestParam(name = "pageSize", defaultValue = "20") @Min(1) Integer pageSize,
            @Parameter(description = "关键词（学员姓名）")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "状态")
            @RequestParam(name = "status", required = false) Integer status
    ) {
        if (status != null && StudentUserStatus.fromCode(status) == null) {
            throw new IllegalArgumentException("status is invalid");
        }
        StudentListRequest request = new StudentListRequest();
        request.setPage(page);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setStatus(status);
        return ApiResponse.success(studentListService.list(request));
    }
}
