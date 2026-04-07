package com.chordsked.backend.service.student;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.student.StudentListRequest;
import com.chordsked.backend.model.vo.student.StudentListResultVO;

public interface StudentListService {
    PageResult<StudentListResultVO> list(StudentListRequest request);
}
