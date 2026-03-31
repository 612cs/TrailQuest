package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.admin.AdminBanUserRequest;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewActionRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewBatchActionRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailManagementPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminUserListItemVo;

public interface AdminService {

    AdminDashboardSummaryVo getDashboardSummary();

    PageResponse<AdminTrailListItemVo> pageTrails(AdminTrailPageRequest request);

    PageResponse<AdminUserListItemVo> pageUsers(AdminUserPageRequest request);

    void banUser(Long userId, Long adminUserId, AdminBanUserRequest request);

    void unbanUser(Long userId, Long adminUserId);

    AdminTrailDetailVo getTrailDetail(Long trailId);

    PageResponse<AdminTrailListItemVo> pageTrailManagement(AdminTrailManagementPageRequest request);

    AdminTrailDetailVo getTrailManagementDetail(Long trailId);

    void approveTrail(Long trailId, Long adminUserId);

    void rejectTrail(Long trailId, Long adminUserId, AdminRejectTrailRequest request);

    void offlineTrail(Long trailId, Long adminUserId);

    void restoreTrail(Long trailId, Long adminUserId);

    PageResponse<AdminReviewListItemVo> pageReviews(AdminReviewPageRequest request);

    AdminReviewDetailVo getReviewDetail(Long reviewId);

    void hideReview(Long reviewId, Long adminUserId, AdminReviewActionRequest request);

    void restoreReview(Long reviewId, Long adminUserId);

    void deleteReview(Long reviewId, Long adminUserId, AdminReviewActionRequest request);

    void batchHideReviews(Long adminUserId, AdminReviewBatchActionRequest request);

    void batchRestoreReviews(Long adminUserId, AdminReviewBatchActionRequest request);

    PageResponse<AdminReportListItemVo> pageReports(long pageNum, long pageSize);

    void resolveReport(Long reportId);
}
