package com.sheng.hikingbackend.dto.admin;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewActionRequest {

    @Size(max = 255, message = "处理原因不能超过255个字符")
    private String remark;
}
