package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewQueryRow {

    private Long id;
    private Long trailId;
    private String text;
    private Integer rating;
    private String status;
    private String moderationReason;
    private LocalDateTime moderatedAt;
    private Long userId;
    private String authorUsername;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private String trailName;
    private Long parentId;
    private String parentText;
    private LocalDateTime createdAt;
}
