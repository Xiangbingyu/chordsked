package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.StudentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("studentMapper")
public interface StudentMapper {
    List<StudentEntity> listStudents(
            @Param("keyword") String keyword,
            @Param("level") String level,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countStudents(
            @Param("keyword") String keyword,
            @Param("level") String level
    );
}

