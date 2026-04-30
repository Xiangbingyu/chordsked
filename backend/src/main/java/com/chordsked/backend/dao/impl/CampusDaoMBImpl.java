package com.chordsked.backend.dao.impl;

import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.dao.mapper.CampusMapper;
import com.chordsked.backend.model.entity.CampusEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Override
    public List<CampusEntity> listNotDeleted() {
        return campusMapper.listNotDeleted();
    }

    @Override
    public int insert(CampusEntity campusEntity) {
        if (campusEntity == null) {
            return 0;
        }
        return campusMapper.insert(campusEntity);
    }

    @Override
    public int updateById(CampusEntity campusEntity) {
        if (campusEntity == null || campusEntity.getId() == null || campusEntity.getId() <= 0) {
            return 0;
        }
        return campusMapper.updateById(campusEntity);
    }

    @Override
    public int deleteById(Long campusId) {
        if (campusId == null || campusId <= 0) {
            return 0;
        }
        return campusMapper.deleteById(campusId);
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return campusMapper.existsByName(name) > 0;
    }

    @Override
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return campusMapper.existsByCode(code) > 0;
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long campusId) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return campusMapper.existsByNameExcludeId(name, campusId) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String code, Long campusId) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return campusMapper.existsByCodeExcludeId(code, campusId) > 0;
    }
}
