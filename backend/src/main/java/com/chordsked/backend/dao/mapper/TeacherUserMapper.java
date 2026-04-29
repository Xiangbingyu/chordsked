package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.TeacherUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("teacherUserMapper")
public interface TeacherUserMapper {
    TeacherUserEntity getById(@Param("userId") Long userId);

    int countByCampusId(@Param("campusId") Long campusId);
}
