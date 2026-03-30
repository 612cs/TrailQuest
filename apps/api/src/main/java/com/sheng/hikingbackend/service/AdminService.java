package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminUserListItemVo;

public interface AdminService {

    AdminDashboardSummaryVo getDashboardSummary();

    PageResponse<AdminTrailListItemVo> pageTrails(AdminTrailPageRequest request);

    PageResponse<AdminUserListItemVo> pageUsers(AdminUserPageRequest request);

    AdminTrailDetailVo getTrailDetail(Long trailId);

    void approveTrail(Long trailId, Long adminUserId);

    void rejectTrail(Long trailId, Long adminUserId, AdminRejectTrailRequest request);

    PageResponse<AdminReviewListItemVo> pageReviews(AdminReviewPageRequest request);

    void deleteReview(Long reviewId);

    PageResponse<AdminReportListItemVo> pageReports(long pageNum, long pageSize);

    void resolveReport(Long reportId);
}
