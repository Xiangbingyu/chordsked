package com.chordsked.backend.service.internaluser.impl;

import com.chordsked.backend.dao.CampusDao;
import com.chordsked.backend.model.entity.CampusEntity;
import com.chordsked.backend.model.vo.internaluser.InternalUserCampusOptionVO;
import com.chordsked.backend.service.internaluser.InternalUserCampusOptionQueryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("internalUserCampusOptionQueryService")
public class InternalUserCampusOptionQueryServiceImpl implements InternalUserCampusOptionQueryService {
    @Resource(name = "campusDao")
    private CampusDao campusDao;

    @Override
    public List<InternalUserCampusOptionVO> list() {
        return campusDao.listNotDeleted().stream()
                .map(this::buildCampusOption)
                .toList();
    }

    private InternalUserCampusOptionVO buildCampusOption(CampusEntity campus) {
        InternalUserCampusOptionVO option = new InternalUserCampusOptionVO();
        option.setId(campus.getId());
        option.setName(campus.getName());
        option.setStatus(campus.getStatus());
        return option;
    }
}
