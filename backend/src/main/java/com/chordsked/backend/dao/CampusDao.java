package com.chordsked.backend.dao;

import com.chordsked.backend.model.entity.CampusEntity;

import java.util.List;

public interface CampusDao {
    CampusEntity getById(Long campusId);

    List<CampusEntity> listNotDeleted();

    int insert(CampusEntity campusEntity);

    int updateById(CampusEntity campusEntity);

    int deleteById(Long campusId);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameExcludeId(String name, Long campusId);

    boolean existsByCodeExcludeId(String code, Long campusId);
}
