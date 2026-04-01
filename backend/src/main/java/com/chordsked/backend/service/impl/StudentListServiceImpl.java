package com.chordsked.backend.service.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.dao.StudentUserDao;
import com.chordsked.backend.model.dto.StudentListRequest;
import com.chordsked.backend.model.entity.StudentUserEntity;
import com.chordsked.backend.model.vo.StudentListResultVO;
import com.chordsked.backend.service.StudentListService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("studentListService")
public class StudentListServiceImpl implements StudentListService {
    private static final Logger logger = LoggerFactory.getLogger(StudentListServiceImpl.class);

    @Resource(name = "studentUserDao")
    private StudentUserDao studentUserDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Override
    public PageResult<StudentListResultVO> list(StudentListRequest request) {
        int page = request.getPage() == null ? 1 : request.getPage();
        int pageSize = request.getPageSize() == null ? 20 : request.getPageSize();
        String keyword = request.getKeyword();
        Integer status = request.getStatus();
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be >= 1");
        }
        if (pageSize > appProperties.getMaxPageSize()) {
            throw new IllegalArgumentException("pageSize too large");
        }
        int offset = (page - 1) * pageSize;
        long total = studentUserDao.countStudents(keyword, status);
        List<StudentUserEntity> entities = studentUserDao.listStudents(keyword, status, offset, pageSize);
        List<StudentListResultVO> items = entities.stream()
                .map(s -> new StudentListResultVO(s.getId(), s.getPhone(), s.getName(), s.getStatus(), s.getCampusId()))
                .collect(Collectors.toList());
        logger.info("List students, page={}, pageSize={}", page, pageSize);
        return PageResult.of(total, items);
    }
}
