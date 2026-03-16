package com.sheng.hikingbackend.vo.review;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewQueryRow {

    private Long id;
    private Long trailId;
    private Long userId;
    private Long parentId;
    private Integer rating;
    private String timeLabel;
    private String text;
    private String replyTo;
    private LocalDateTime createdAt;
    private String username;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
}
