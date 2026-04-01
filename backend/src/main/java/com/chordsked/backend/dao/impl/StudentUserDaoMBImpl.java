package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.StudentUserDao;
import com.chordsked.backend.dao.mapper.StudentUserMapper;
import com.chordsked.backend.model.entity.StudentUserEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("studentUserDao")
public class StudentUserDaoMBImpl implements StudentUserDao {

    @Resource(name = "studentUserMapper")
    private StudentUserMapper studentUserMapper;

    @Override
    public StudentUserEntity getById(Long id) {
        return studentUserMapper.getById(id);
    }

    @Override
    public List<StudentUserEntity> listStudents(String keyword, Integer status, int offset, int limit) {
        return studentUserMapper.listStudents(keyword, status, offset, limit);
    }

    @Override
    public long countStudents(String keyword, Integer status) {
        return studentUserMapper.countStudents(keyword, status);
    }

    @Override
    public int insertStudent(StudentUserEntity studentUserEntity) {
        return studentUserMapper.insertStudent(studentUserEntity);
    }

    @Override
    public int updateStudent(StudentUserEntity studentUserEntity) {
        return studentUserMapper.updateStudent(studentUserEntity);
    }

    @Override
    public int deleteStudent(Long id) {
        return studentUserMapper.deleteStudent(id);
    }
}
