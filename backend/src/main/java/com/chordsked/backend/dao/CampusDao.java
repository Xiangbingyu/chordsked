package com.chordsked.backend.dao;

import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;

import java.util.List;

public interface CampusDao {
    CampusEntity getById(Long campusId);

    List<CampusEntity> listNotDeleted();

    List<CampusQueryResultVO> listByQuery(CampusQueryRequest request);

    int countByQuery(CampusQueryRequest request);

    CampusDetailResultVO getDetailById(Long campusId);

    int insert(CampusEntity campusEntity);

    int updateById(CampusEntity campusEntity);

    int deleteById(Long campusId);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameExcludeId(String name, Long campusId);

    boolean existsByCodeExcludeId(String code, Long campusId);
}
