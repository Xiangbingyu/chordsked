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
        if (id == null || id <= 0) {
            return null;
        }
        return studentUserMapper.getById(id);
    }

    @Override
    public List<StudentUserEntity> listStudents(String keyword, Integer status, int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        return studentUserMapper.listStudents(keyword, status, offset, limit);
    }

    @Override
    public long countStudents(String keyword, Integer status) {
        return studentUserMapper.countStudents(keyword, status);
    }

    @Override
    public int insertStudent(StudentUserEntity studentUserEntity) {
        if (studentUserEntity == null) {
            throw new IllegalArgumentException("studentUserEntity must not be null");
        }
        return studentUserMapper.insertStudent(studentUserEntity);
    }

    @Override
    public int updateStudent(StudentUserEntity studentUserEntity) {
        if (studentUserEntity == null || studentUserEntity.getId() == null || studentUserEntity.getId() <= 0) {
            throw new IllegalArgumentException("studentUserEntity id is invalid");
        }
        return studentUserMapper.updateStudent(studentUserEntity);
    }

    @Override
    public int deleteStudent(Long id, Long updatedAt) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id is invalid");
        }
        if (updatedAt == null || updatedAt <= 0) {
            throw new IllegalArgumentException("updatedAt is invalid");
        }
        return studentUserMapper.deleteStudent(id, updatedAt);
    }
}
