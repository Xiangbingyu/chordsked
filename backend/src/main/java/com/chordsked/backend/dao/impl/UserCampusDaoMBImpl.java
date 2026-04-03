package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.dao.mapper.UserCampusMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("userCampusDao")
public class UserCampusDaoMBImpl implements UserCampusDao {
    @Resource(name = "userCampusMapper")
    private UserCampusMapper userCampusMapper;

    @Override
    public Long getPrimaryCampusIdByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return userCampusMapper.selectPrimaryCampusIdByUserId(userId);
    }
}
