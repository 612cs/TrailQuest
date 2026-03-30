package com.sheng.hikingbackend.vo.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryVo {

    private long pendingTrailCount;
    private long reviewCount;
    private long pendingReportCount;
}
