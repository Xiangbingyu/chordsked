package com.chordsked.backend.service.campus;

public interface CampusWriteValidator {
    String validateAndNormalizeCode(String code);

    String validateAndNormalizeName(String name);

    Integer validateStatus(Integer status);

    void validateCodeForCreate(String code);

    void validateNameForCreate(String name);

    void validateCodeForUpdate(String code, Long campusId);

    void validateNameForUpdate(String name, Long campusId);
}
