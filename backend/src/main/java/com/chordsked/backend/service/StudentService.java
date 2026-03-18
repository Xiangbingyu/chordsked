package com.chordsked.backend.service;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.model.vo.StudentVO;
public interface StudentService {
    PageResult<StudentVO> listStudents(String keyword, String level, int page, int pageSize);
}
