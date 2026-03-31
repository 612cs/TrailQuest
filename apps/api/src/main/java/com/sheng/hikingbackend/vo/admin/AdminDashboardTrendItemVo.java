package com.sheng.hikingbackend.vo.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardTrendItemVo {

    private String date;
    private long newTrailCount;
    private long newReviewCount;
    private long newReportCount;
    private long newUserCount;
}
