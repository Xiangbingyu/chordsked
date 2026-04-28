package com.chordsked.backend.service.internaluser;

import java.util.List;

public interface InternalUserWriteValidator {
    List<Long> validateRoleIds(List<Long> roleIds);

    List<Long> validateCampusIds(List<Long> campusIds, Long primaryCampusId);
}
