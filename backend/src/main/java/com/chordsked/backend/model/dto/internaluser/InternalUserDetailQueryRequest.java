package com.chordsked.backend.model.dto.internaluser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InternalUserDetailQueryRequest", description = "教务端账号详情查询请求")
public class InternalUserDetailQueryRequest {
    @Schema(description = "用户ID", example = "1001")
    @NotNull
    @Min(1)
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
