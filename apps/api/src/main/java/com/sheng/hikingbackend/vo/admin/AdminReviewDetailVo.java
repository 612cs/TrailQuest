package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReviewDetailVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long trailId;

    private String trailName;
    private Integer rating;
    private String text;
    private String status;
    private String moderationReason;
    private LocalDateTime moderatedAt;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String authorUsername;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    private String parentText;
    private List<AdminReviewThreadItemVo> replies;
    private LocalDateTime createdAt;
}
