package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.CampusEntity;

public interface CampusDao {
    CampusEntity getById(Long campusId);
}
