package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReviewListItemVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long trailId;
    private String trailName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String authorUsername;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private Integer rating;
    private String text;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    private LocalDateTime createdAt;
}
