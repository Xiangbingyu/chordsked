package com.chordsked.backend.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("userCampusMapper")
public interface UserCampusMapper {
    Long selectPrimaryCampusIdByUserId(@Param("userId") Long userId);
}
