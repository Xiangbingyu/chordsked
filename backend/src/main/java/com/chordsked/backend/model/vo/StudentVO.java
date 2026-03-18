package com.chordsked.backend.model.vo;

public class StudentVO {
    private Long id;
    private String name;
    private Integer age;
    private String level;

    public StudentVO() {
    }

    public StudentVO(Long id, String name, Integer age, String level) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
