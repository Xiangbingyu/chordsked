package com.chordsked.backend.model.vo;

import com.chordsked.backend.model.enums.StudentUserStatus;

public class StudentListResultVO {
    private Long id;
    private String phone;
    private String name;
    private StudentUserStatus status;
    private Long campusId;

    public StudentListResultVO() {
    }

    public StudentListResultVO(Long id, String phone, String name, StudentUserStatus status, Long campusId) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.status = status;
        this.campusId = campusId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StudentUserStatus getStatus() {
        return status;
    }

    public void setStatus(StudentUserStatus status) {
        this.status = status;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}
