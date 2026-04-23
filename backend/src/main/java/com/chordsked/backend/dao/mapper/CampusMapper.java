package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.CampusEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("campusMapper")
public interface CampusMapper {
    CampusEntity getById(@Param("campusId") Long campusId);
}
