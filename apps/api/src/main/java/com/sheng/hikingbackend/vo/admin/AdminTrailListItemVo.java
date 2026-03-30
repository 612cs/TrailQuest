package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTrailListItemVo {

    private Long id;
    private String image;
    private String name;
    private String location;
    private String reviewStatus;
    private String authorUsername;
    private LocalDateTime createdAt;
}
