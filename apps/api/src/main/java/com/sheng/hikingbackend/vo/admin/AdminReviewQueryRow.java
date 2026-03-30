package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewQueryRow {

    private Long id;
    private String text;
    private String authorUsername;
    private String trailName;
    private LocalDateTime createdAt;
}
