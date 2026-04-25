package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOperationLogListItemVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    private String operatorName;
    private String operatorRole;
    private String moduleCode;
    private String actionCode;
    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    private String targetTitle;
    private String reason;
    private String resultStatus;
    private LocalDateTime createdAt;
}
