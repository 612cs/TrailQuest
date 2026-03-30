package com.sheng.hikingbackend.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminHomeHeroUpdateRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.service.AdminService;
import com.sheng.hikingbackend.service.AuthService;
import com.sheng.hikingbackend.service.SiteSettingService;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminUserListItemVo;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.site.HomeHeroSettingVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;
    private final SiteSettingService siteSettingService;

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.getCurrentUser(userDetails.getId()));
    }

    @GetMapping("/dashboard/summary")
    public ApiResponse<AdminDashboardSummaryVo> summary() {
        return ApiResponse.success(adminService.getDashboardSummary());
    }

    @GetMapping("/trails")
    public ApiResponse<PageResponse<AdminTrailListItemVo>> pageTrails(@Valid AdminTrailPageRequest request) {
        return ApiResponse.success(adminService.pageTrails(request));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserListItemVo>> pageUsers(@Valid AdminUserPageRequest request) {
        return ApiResponse.success(adminService.pageUsers(request));
    }

    @GetMapping("/trails/{id}")
    public ApiResponse<AdminTrailDetailVo> trailDetail(@PathVariable Long id) {
        return ApiResponse.success(adminService.getTrailDetail(id));
    }

    @PostMapping("/trails/{id}/approve")
    public ApiResponse<Void> approveTrail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        adminService.approveTrail(id, userDetails.getId());
        return ApiResponse.success("审核通过", null);
    }

    @PostMapping("/trails/{id}/reject")
    public ApiResponse<Void> rejectTrail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminRejectTrailRequest request) {
        adminService.rejectTrail(id, userDetails.getId(), request);
        return ApiResponse.success("已驳回路线", null);
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResponse<AdminReviewListItemVo>> pageReviews(@Valid AdminReviewPageRequest request) {
        return ApiResponse.success(adminService.pageReviews(request));
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {
        adminService.deleteReview(id);
        return ApiResponse.success("评论删除成功", null);
    }

    @GetMapping("/reports")
    public ApiResponse<PageResponse<AdminReportListItemVo>> pageReports(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(adminService.pageReports(pageNum, pageSize));
    }

    @GetMapping("/settings/home-hero")
    public ApiResponse<HomeHeroSettingVo> getHomeHeroSetting() {
        return ApiResponse.success(siteSettingService.getHomeHeroSetting());
    }

    @PostMapping("/settings/home-hero")
    public ApiResponse<Void> updateHomeHeroSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminHomeHeroUpdateRequest request) {
        siteSettingService.updateHomeHeroImage(userDetails.getId(), request.getImageUrl());
        return ApiResponse.success("首页大屏图片已更新", null);
    }

    @PostMapping("/reports/{id}/resolve")
    public ApiResponse<Void> resolveReport(@PathVariable Long id) {
        adminService.resolveReport(id);
        return ApiResponse.success("举报处理成功", null);
    }
}
