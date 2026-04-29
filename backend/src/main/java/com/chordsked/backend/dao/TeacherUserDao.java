package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.TeacherUserEntity;

public interface TeacherUserDao {
    TeacherUserEntity getById(Long userId);

    int countByCampusId(Long campusId);
}
