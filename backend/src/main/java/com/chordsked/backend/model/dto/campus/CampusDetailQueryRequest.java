package com.chordsked.backend.model.dto.campus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CampusDetailQueryRequest", description = "校区详情查询参数")
public class CampusDetailQueryRequest {
    @NotNull
    private Long campusId;

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
}
