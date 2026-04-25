package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardRiskItemVo {

    private String type;
    private String title;
    private String description;
    private String targetType;
    private String targetId;
    private String targetTitle;
    private String priority;
    private LocalDateTime createdAt;
}
