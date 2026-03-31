package com.sheng.hikingbackend.dto.admin;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewBatchActionRequest {

    @NotEmpty(message = "评论ID不能为空")
    private List<Long> ids;

    @Size(max = 255, message = "处理原因不能超过255个字符")
    private String remark;
}
