package com.chordsked.backend.dao;

import java.util.List;

public interface UserCampusDao {
    Long getPrimaryCampusIdByUserId(Long userId);

    List<Long> listCampusIdsByUserId(Long userId);

    int deleteByUserId(Long userId);

    int saveBatch(List<Long> campusIds, Long primaryCampusId, Long userId, Long now);

    int countByCampusId(Long campusId);
}
