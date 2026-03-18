package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.StudentEntity;

import java.util.List;

public interface StudentDao {
    List<StudentEntity> listStudents(
            String keyword,
            String level,
            int offset,
            int limit
    );

    long countStudents(
            String keyword,
            String level
    );
}
