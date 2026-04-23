package com.chordsked.backend.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("userCampusMapper")
public interface UserCampusMapper {
    Long selectPrimaryCampusIdByUserId(@Param("userId") Long userId);

    List<Long> listCampusIdsByUserId(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);

    int saveBatch(@Param("campusIds") List<Long> campusIds, @Param("primaryCampusId") Long primaryCampusId, @Param("userId") Long userId, @Param("now") Long now);
}
