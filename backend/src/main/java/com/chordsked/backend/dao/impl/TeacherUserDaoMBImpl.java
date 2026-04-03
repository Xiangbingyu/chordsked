package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.TeacherUserDao;
import com.chordsked.backend.dao.mapper.TeacherUserMapper;
import com.chordsked.backend.model.entity.TeacherUserEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("teacherUserDao")
public class TeacherUserDaoMBImpl implements TeacherUserDao {
    @Resource(name = "teacherUserMapper")
    private TeacherUserMapper teacherUserMapper;

    @Override
    public TeacherUserEntity getById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return teacherUserMapper.getById(userId);
    }
}
