package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.dto.role.RoleQueryRequest;
import com.chordsked.backend.model.entity.RoleEntity;
import com.chordsked.backend.model.vo.role.RoleDetailQueryResultVO;
import com.chordsked.backend.model.vo.role.RoleQueryResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository("roleMapper")
public interface RoleMapper {
    RoleEntity getById(@Param("id") Long id);

    RoleEntity getByCode(@Param("code") String code);

    RoleDetailQueryResultVO getDetailById(@Param("id") Long id);

    int save(@Param("role") RoleEntity role);

    Long countUserBinding(@Param("roleId") Long roleId);

    int updateById(@Param("role") RoleEntity role);

    int deleteById(@Param("roleId") Long roleId);

    List<RoleQueryResultVO> listByQuery(@Param("request") RoleQueryRequest request,
                                        @Param("excludedRoleCodes") List<String> excludedRoleCodes);

    Long countByQuery(@Param("request") RoleQueryRequest request,
                      @Param("excludedRoleCodes") List<String> excludedRoleCodes);
}
