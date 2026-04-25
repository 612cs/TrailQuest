package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOperationLogQueryRow {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorRole;
    private String moduleCode;
    private String actionCode;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String reason;
    private String resultStatus;
    private String beforeSnapshot;
    private String afterSnapshot;
    private String requestId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
