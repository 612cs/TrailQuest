package com.sheng.hikingbackend.dto.review;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {

    @NotNull(message = "路线 ID 不能为空")
    private Long trailId;

    private Long parentId;

    @Min(value = 1, message = "评分不能小于 1")
    @Max(value = 5, message = "评分不能大于 5")
    private Integer rating;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过 2000 个字符")
    private String text;

    @Size(max = 50, message = "回复对象过长")
    private String replyTo;

    @Size(max = 9, message = "最多上传 9 张图片")
    private List<@Size(max = 500, message = "图片地址过长") String> images;
}
