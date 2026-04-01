package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.StudentUserEntity;

import java.util.List;

public interface StudentUserDao {
    StudentUserEntity getById(Long id);

    List<StudentUserEntity> listStudents(
            String keyword,
            Integer status,
            int offset,
            int limit
    );

    long countStudents(
            String keyword,
            Integer status
    );

    int insertStudent(StudentUserEntity studentUserEntity);

    int updateStudent(StudentUserEntity studentUserEntity);

    int deleteStudent(Long id);
}
