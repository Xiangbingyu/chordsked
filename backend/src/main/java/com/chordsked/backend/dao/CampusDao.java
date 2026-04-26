package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.CampusEntity;

import java.util.List;

public interface CampusDao {
    CampusEntity getById(Long campusId);

    List<CampusEntity> listNotDeleted();
}
