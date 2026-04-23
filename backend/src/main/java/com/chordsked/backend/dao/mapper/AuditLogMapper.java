package com.chordsked.backend.dao.mapper;

import com.chordsked.backend.model.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("auditLogMapper")
public interface AuditLogMapper {
    int save(@Param("auditLog") AuditLogEntity auditLog);
}
