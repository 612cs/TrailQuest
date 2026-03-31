package com.sheng.hikingbackend.vo.admin;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryVo {

    private long pendingTrailCount;
    private long pendingReportCount;
    private long hiddenReviewCount;
    private long todayNewUserCount;
    private long todayNewTrailCount;
    private long todayNewReviewCount;
    private long offlineTrailCount;
    private long reportedReviewCount;
    private List<AdminDashboardTrendItemVo> trends;
    private List<AdminDashboardRiskItemVo> recentRisks;
}
