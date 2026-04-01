package com.chordsked.backend.service;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.dto.StudentListRequest;
import com.chordsked.backend.model.vo.StudentListResultVO;

public interface StudentListService {
    PageResult<StudentListResultVO> list(StudentListRequest request);
}
