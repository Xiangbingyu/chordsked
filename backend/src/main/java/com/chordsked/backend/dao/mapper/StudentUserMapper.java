package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.StudentUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("studentUserMapper")
public interface StudentUserMapper {
    StudentUserEntity getById(@Param("id") Long id);

    List<StudentUserEntity> listStudents(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long countStudents(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    int insertStudent(StudentUserEntity studentUserEntity);

    int updateStudent(StudentUserEntity studentUserEntity);

    int deleteStudent(@Param("id") Long id, @Param("updatedAt") Long updatedAt);
}
