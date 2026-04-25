package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardRiskQueryRow {

    private String actionCode;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String reason;
    private LocalDateTime createdAt;
}
