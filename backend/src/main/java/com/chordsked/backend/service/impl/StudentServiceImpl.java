package com.chordsked.backend.service.impl;

import com.chordsked.backend.common.PageResult;
import com.chordsked.backend.config.properties.AppProperties;
import com.chordsked.backend.dao.StudentDao;
import com.chordsked.backend.model.entity.StudentEntity;
import com.chordsked.backend.model.vo.StudentVO;
import com.chordsked.backend.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;

@Service("studentService")
public class StudentServiceImpl implements StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Resource(name = "studentDao")
    private StudentDao studentDao;

    @Resource(name = "appProperties")
    private AppProperties appProperties;

    @Override
    public PageResult<StudentVO> listStudents(String keyword, String level, int page, int pageSize) {
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
        long total = studentDao.countStudents(keyword, level);
        List<StudentEntity> entities = studentDao.listStudents(keyword, level, offset, pageSize);
        List<StudentVO> items = entities.stream()
                .map(s -> new StudentVO(s.getId(), s.getName(), s.getAge(), s.getLevel()))
                .collect(Collectors.toList());

        logger.info("List students, page={}, pageSize={}", page, pageSize);
        return PageResult.of(total, items);
    }
}
