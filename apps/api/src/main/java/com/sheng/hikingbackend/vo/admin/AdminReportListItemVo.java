package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportListItemVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
}
