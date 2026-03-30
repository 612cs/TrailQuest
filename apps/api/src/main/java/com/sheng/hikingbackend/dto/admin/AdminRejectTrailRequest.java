package com.sheng.hikingbackend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRejectTrailRequest {

    @NotBlank(message = "驳回原因不能为空")
    private String remark;
}
