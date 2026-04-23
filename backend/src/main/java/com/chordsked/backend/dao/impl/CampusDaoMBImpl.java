package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.mapper.CampusMapper;
import com.chordsked.backend.model.entity.CampusEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository("campusDao")
public class CampusDaoMBImpl implements CampusDao {
    @Resource(name = "campusMapper")
    private CampusMapper campusMapper;

    @Override
    public CampusEntity getById(Long campusId) {
        if (campusId == null || campusId <= 0) {
            return null;
        }
        return campusMapper.getById(campusId);
    }
}
