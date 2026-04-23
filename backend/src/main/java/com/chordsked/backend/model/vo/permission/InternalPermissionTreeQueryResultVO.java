package com.chordsked.backend.model.vo.permission;

import java.util.ArrayList;
import java.util.List;

public class InternalPermissionTreeQueryResultVO {
    private Long id;
    private String code;
    private String name;
    private Integer type;
    private String path;
    private Integer sort;
    private List<InternalPermissionTreeQueryResultVO> children;

    public InternalPermissionTreeQueryResultVO() {
        this.children = new ArrayList<>();
    }

    public InternalPermissionTreeQueryResultVO(Long id, String code, String name, Integer type, String path, Integer sort) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.path = path;
        this.sort = sort;
        this.children = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public List<InternalPermissionTreeQueryResultVO> getChildren() {
        return children;
    }

    public void setChildren(List<InternalPermissionTreeQueryResultVO> children) {
        this.children = children;
    }
}
