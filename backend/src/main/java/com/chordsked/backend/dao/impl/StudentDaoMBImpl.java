package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.StudentDao;
import com.chordsked.backend.dao.mapper.StudentMapper;
import com.chordsked.backend.model.entity.StudentEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("studentDao")
public class StudentDaoMBImpl implements StudentDao {

    @Resource(name = "studentMapper")
    private StudentMapper studentMapper;

    @Override
    public List<StudentEntity> listStudents(String keyword, String level, int offset, int limit) {
        return studentMapper.listStudents(keyword, level, offset, limit);
    }

    @Override
    public long countStudents(String keyword, String level) {
        return studentMapper.countStudents(keyword, level);
    }
}
