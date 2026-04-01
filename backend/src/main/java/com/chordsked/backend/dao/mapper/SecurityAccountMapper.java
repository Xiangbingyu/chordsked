package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.InternalUserEntity;
import com.chordsked.backend.model.entity.StudentUserEntity;
import com.chordsked.backend.model.entity.TeacherUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("securityAccountMapper")
public interface SecurityAccountMapper {
    InternalUserEntity selectInternalUserById(@Param("userId") Long userId);

    TeacherUserEntity selectTeacherUserById(@Param("userId") Long userId);

    StudentUserEntity selectStudentUserById(@Param("userId") Long userId);

    Long selectPrimaryCampusIdByInternalUserId(@Param("userId") Long userId);

    List<String> selectPermissionCodesByInternalUserId(@Param("userId") Long userId);

    List<String> selectExistingPermissionCodes(@Param("codes") List<String> codes);
}
