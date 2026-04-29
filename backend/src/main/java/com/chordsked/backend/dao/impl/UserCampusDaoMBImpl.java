package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.UserCampusDao;
import com.chordsked.backend.dao.mapper.UserCampusMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<Long> listCampusIdsByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return List.of();
        }
        return userCampusMapper.listCampusIdsByUserId(userId);
    }

    @Override
    public int deleteByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            return 0;
        }
        return userCampusMapper.deleteByUserId(userId);
    }

    @Override
    public int saveBatch(List<Long> campusIds, Long primaryCampusId, Long userId, Long now) {
        if (campusIds == null || campusIds.isEmpty() || userId == null || userId <= 0 || now == null || now <= 0) {
            return 0;
        }
        return userCampusMapper.saveBatch(campusIds, primaryCampusId, userId, now);
    }

    @Override
    public int countByCampusId(Long campusId) {
        if (campusId == null || campusId <= 0) {
            return 0;
        }
        return userCampusMapper.countByCampusId(campusId);
    }
}
