package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReviewThreadItemVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String authorUsername;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private String text;
    private String status;
    private String moderationReason;
    private LocalDateTime moderatedAt;
    private LocalDateTime createdAt;
}
