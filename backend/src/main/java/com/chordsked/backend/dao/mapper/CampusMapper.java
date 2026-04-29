package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.datascope.annotation.DataScope;
import com.chordsked.backend.model.dto.campus.CampusQueryRequest;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.vo.campus.CampusDetailResultVO;
import com.chordsked.backend.model.vo.campus.CampusQueryResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("campusMapper")
public interface CampusMapper {
    CampusEntity getById(@Param("campusId") Long campusId);

    List<CampusEntity> listNotDeleted();

    @DataScope(tableAlias = "c", scopeField = "id")
    List<CampusQueryResultVO> listByQuery(@Param("request") CampusQueryRequest request);

    @DataScope(tableAlias = "c", scopeField = "id")
    int countByQuery(@Param("request") CampusQueryRequest request);

    CampusDetailResultVO getDetailById(@Param("campusId") Long campusId);

    int insert(@Param("entity") CampusEntity campusEntity);

    int updateById(@Param("entity") CampusEntity campusEntity);

    int deleteById(@Param("campusId") Long campusId);

    int existsByName(@Param("name") String name);

    int existsByCode(@Param("code") String code);

    int existsByNameExcludeId(@Param("name") String name, @Param("campusId") Long campusId);

    int existsByCodeExcludeId(@Param("code") String code, @Param("campusId") Long campusId);
}
