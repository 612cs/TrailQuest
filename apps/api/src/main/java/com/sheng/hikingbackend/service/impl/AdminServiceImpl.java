package com.sheng.hikingbackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.enums.UserStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminBanUserRequest;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewActionRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewBatchActionRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailManagementPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.entity.Review;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.AdminOperationLogMapper;
import com.sheng.hikingbackend.mapper.ReviewMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AdminService;
import com.sheng.hikingbackend.service.AdminOperationLogService;
import com.sheng.hikingbackend.service.ReviewService;
import com.sheng.hikingbackend.vo.admin.AdminDashboardDailyCountRow;
import com.sheng.hikingbackend.vo.admin.AdminDashboardRiskItemVo;
import com.sheng.hikingbackend.vo.admin.AdminDashboardRiskQueryRow;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminDashboardTrendItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewQueryRow;
import com.sheng.hikingbackend.vo.admin.AdminReviewThreadItemVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminUserListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminUserQueryRow;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailGalleryItemVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;
import com.sheng.hikingbackend.vo.trail.TrailTrackVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String REVIEW_STATUS_ACTIVE = "active";
    private static final String REVIEW_STATUS_HIDDEN = "hidden";
    private static final String REVIEW_STATUS_DELETED = "deleted";

    private final TrailMapper trailMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;
    private final TrailImageMapper trailImageMapper;
    private final TrailTrackMapper trailTrackMapper;
    private final UserMapper userMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final AdminOperationLogService adminOperationLogService;

    @Override
    public AdminDashboardSummaryVo getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime trendStartAt = startDate.atStartOfDay();

        return AdminDashboardSummaryVo.builder()
                .pendingTrailCount(trailMapper.countPendingReviewTrails())
                .pendingReportCount(0)
                .hiddenReviewCount(reviewMapper.countHiddenReviews())
                .todayNewUserCount(defaultLong(userMapper.countTodayNewUsers()))
                .todayNewTrailCount(trailMapper.countTodayNewTrails())
                .todayNewReviewCount(reviewMapper.countTodayNewReviews())
                .offlineTrailCount(trailMapper.countOfflineTrails())
                .reportedReviewCount(0)
                .trends(buildDashboardTrends(startDate, today, trendStartAt))
                .recentRisks(buildRecentRisks())
                .build();
    }

    @Override
    public PageResponse<AdminTrailListItemVo> pageTrails(AdminTrailPageRequest request) {
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> result = trailMapper.selectAdminTrailPage(page, request);
        return PageResponse.of(result.getRecords().stream().map(this::toAdminTrailListItem).toList(),
                result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public PageResponse<AdminUserListItemVo> pageUsers(AdminUserPageRequest request) {
        Page<AdminUserQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<AdminUserQueryRow> result = userMapper.selectAdminUserPage(page, request);
        List<AdminUserListItemVo> list = result.getRecords().stream()
                .map(row -> AdminUserListItemVo.builder()
                        .id(row.getId())
                        .username(row.getUsername())
                        .email(row.getEmail())
                        .role(row.getRole())
                        .location(row.getLocation())
                        .avatar(row.getAvatar())
                        .avatarBg(row.getAvatarBg())
                        .avatarMediaUrl(row.getAvatarMediaUrl())
                        .status(row.getStatus())
                        .bannedAt(row.getBannedAt())
                        .publishedTrailCount(row.getPublishedTrailCount())
                        .createdAt(row.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    @Transactional
    public void banUser(Long userId, Long adminUserId, AdminBanUserRequest request) {
        User user = requireUser(userId);
        if (userId.equals(adminUserId)) {
            throw BusinessException.badRequest("ADMIN_SELF_BAN_FORBIDDEN", "不能封禁当前登录管理员");
        }
        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw BusinessException.badRequest("USER_ALREADY_BANNED", "该用户已被封禁");
        }
        if (UserStatus.DELETED.getCode().equals(user.getStatus())) {
            throw BusinessException.badRequest("USER_ALREADY_DELETED", "该用户当前不可恢复");
        }

        user.setStatus(UserStatus.BANNED.getCode());
        user.setBanReason(request.getReason());
        user.setBannedBy(adminUserId);
        user.setBannedAt(LocalDateTime.now());
        userMapper.updateById(user);

        adminOperationLogService.record(
                adminUserId,
                "user_management",
                "user_ban",
                "user",
                userId,
                user.getUsername(),
                request.getReason(),
                snapshot("status", UserStatus.ACTIVE.getCode()),
                snapshot(
                        "status", user.getStatus(),
                        "banReason", user.getBanReason()));
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long adminUserId) {
        User user = requireUser(userId);
        if (!UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw BusinessException.badRequest("USER_NOT_BANNED", "该用户当前不是封禁状态");
        }

        user.setStatus(UserStatus.ACTIVE.getCode());
        user.setBanReason(null);
        user.setBannedBy(null);
        user.setBannedAt(null);
        userMapper.updateById(user);

        adminOperationLogService.record(
                adminUserId,
                "user_management",
                "user_unban",
                "user",
                userId,
                user.getUsername(),
                "恢复账号",
                snapshot("status", UserStatus.BANNED.getCode()),
                snapshot("status", user.getStatus()));
    }

    @Override
    public AdminTrailDetailVo getTrailDetail(Long trailId) {
        TrailQueryRow row = requireTrailRow(trailId);
        if (!"active".equals(row.getStatus())) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toAdminTrailDetail(row);
    }

    @Override
    public PageResponse<AdminTrailListItemVo> pageTrailManagement(AdminTrailManagementPageRequest request) {
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> result = trailMapper.selectAdminTrailManagementPage(page, request);
        return PageResponse.of(result.getRecords().stream().map(this::toAdminTrailListItem).toList(),
                result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public AdminTrailDetailVo getTrailManagementDetail(Long trailId) {
        return toAdminTrailDetail(requireTrailRow(trailId));
    }

    @Override
    @Transactional
    public void approveTrail(Long trailId, Long adminUserId) {
        Trail trail = requireTrail(trailId);
        trail.setReviewStatus(TrailReviewStatus.APPROVED.getCode());
        trail.setReviewRemark(null);
        trail.setReviewedBy(adminUserId);
        trail.setReviewedAt(LocalDateTime.now());
        trailMapper.updateById(trail);

        adminOperationLogService.record(
                adminUserId,
                "trail_review",
                "trail_approve",
                "trail",
                trailId,
                trail.getName(),
                "审核通过",
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", TrailReviewStatus.PENDING.getCode()),
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", trail.getReviewStatus()));
    }

    @Override
    @Transactional
    public void rejectTrail(Long trailId, Long adminUserId, AdminRejectTrailRequest request) {
        Trail trail = requireTrail(trailId);
        String remark = request.getRemark().trim();
        trail.setReviewStatus(TrailReviewStatus.REJECTED.getCode());
        trail.setReviewRemark(remark);
        trail.setReviewedBy(adminUserId);
        trail.setReviewedAt(LocalDateTime.now());
        trailMapper.updateById(trail);

        adminOperationLogService.record(
                adminUserId,
                "trail_review",
                "trail_reject",
                "trail",
                trailId,
                trail.getName(),
                remark,
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", TrailReviewStatus.PENDING.getCode(),
                        "reviewRemark", null),
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", trail.getReviewStatus(),
                        "reviewRemark", trail.getReviewRemark()));
    }

    @Override
    @Transactional
    public void offlineTrail(Long trailId, Long adminUserId) {
        Trail trail = requireExistingTrail(trailId);
        if (!TrailReviewStatus.APPROVED.getCode().equals(trail.getReviewStatus())) {
            throw BusinessException.badRequest("TRAIL_NOT_APPROVED", "仅已通过审核的路线可以下架");
        }
        if ("deleted".equals(trail.getStatus())) {
            throw BusinessException.badRequest("TRAIL_ALREADY_OFFLINE", "该路线已下架");
        }

        trail.setStatus("deleted");
        trailMapper.updateById(trail);

        adminOperationLogService.record(
                adminUserId,
                "trail_management",
                "trail_offline",
                "trail",
                trailId,
                trail.getName(),
                "路线下架",
                snapshot(
                        "status", "active",
                        "reviewStatus", trail.getReviewStatus()),
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", trail.getReviewStatus()));
    }

    @Override
    @Transactional
    public void restoreTrail(Long trailId, Long adminUserId) {
        Trail trail = requireExistingTrail(trailId);
        if (!"deleted".equals(trail.getStatus())) {
            throw BusinessException.badRequest("TRAIL_NOT_OFFLINE", "该路线当前不是下架状态");
        }

        trail.setStatus("active");
        trailMapper.updateById(trail);

        adminOperationLogService.record(
                adminUserId,
                "trail_management",
                "trail_restore",
                "trail",
                trailId,
                trail.getName(),
                "路线恢复",
                snapshot(
                        "status", "deleted",
                        "reviewStatus", trail.getReviewStatus()),
                snapshot(
                        "status", trail.getStatus(),
                        "reviewStatus", trail.getReviewStatus()));
    }

    @Override
    public PageResponse<AdminReviewListItemVo> pageReviews(AdminReviewPageRequest request) {
        Page<AdminReviewQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<AdminReviewQueryRow> result = reviewMapper.selectAdminReviewPage(page, request);
        List<AdminReviewListItemVo> list = result.getRecords().stream()
                .map(row -> AdminReviewListItemVo.builder()
                        .id(row.getId())
                        .trailId(row.getTrailId())
                        .userId(row.getUserId())
                        .text(row.getText())
                        .rating(row.getRating())
                        .status(row.getStatus())
                        .parentId(row.getParentId())
                        .parentText(row.getParentText())
                        .moderationReason(row.getModerationReason())
                        .moderatedAt(row.getModeratedAt())
                        .authorUsername(row.getAuthorUsername())
                        .avatar(row.getAvatar())
                        .avatarBg(row.getAvatarBg())
                        .avatarMediaUrl(row.getAvatarMediaUrl())
                        .trailName(row.getTrailName())
                        .createdAt(row.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    @Transactional
    public AdminReviewDetailVo getReviewDetail(Long reviewId) {
        AdminReviewQueryRow row = requireReviewRow(reviewId);
        List<AdminReviewThreadItemVo> replies = reviewMapper.selectAdminReviewRepliesByParentId(reviewId).stream()
                .map(this::toAdminReviewThreadItem)
                .toList();
        return AdminReviewDetailVo.builder()
                .id(row.getId())
                .trailId(row.getTrailId())
                .trailName(row.getTrailName())
                .rating(row.getRating())
                .text(row.getText())
                .status(row.getStatus())
                .moderationReason(row.getModerationReason())
                .moderatedAt(row.getModeratedAt())
                .userId(row.getUserId())
                .authorUsername(row.getAuthorUsername())
                .avatar(row.getAvatar())
                .avatarBg(row.getAvatarBg())
                .avatarMediaUrl(row.getAvatarMediaUrl())
                .parentId(row.getParentId())
                .parentText(row.getParentText())
                .replies(replies)
                .createdAt(row.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void hideReview(Long reviewId, Long adminUserId, AdminReviewActionRequest request) {
        String remark = normalizeRequiredRemark(request == null ? null : request.getRemark(), "隐藏评论时必须填写处理原因");
        moderateReview(reviewId, adminUserId, REVIEW_STATUS_HIDDEN, remark, "review.hide");
    }

    @Override
    @Transactional
    public void restoreReview(Long reviewId, Long adminUserId) {
        moderateReview(reviewId, adminUserId, REVIEW_STATUS_ACTIVE, "恢复显示", "review.restore");
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long adminUserId, AdminReviewActionRequest request) {
        String remark = normalizeRequiredRemark(request == null ? null : request.getRemark(), "删除评论时必须填写处理原因");
        moderateReview(reviewId, adminUserId, REVIEW_STATUS_DELETED, remark, "review.delete");
    }

    @Override
    @Transactional
    public void batchHideReviews(Long adminUserId, AdminReviewBatchActionRequest request) {
        String remark = normalizeRequiredRemark(request == null ? null : request.getRemark(), "批量隐藏时必须填写处理原因");
        for (Long reviewId : request.getIds()) {
            moderateReview(reviewId, adminUserId, REVIEW_STATUS_HIDDEN, remark, "review.hide");
        }
    }

    @Override
    @Transactional
    public void batchRestoreReviews(Long adminUserId, AdminReviewBatchActionRequest request) {
        for (Long reviewId : request.getIds()) {
            moderateReview(reviewId, adminUserId, REVIEW_STATUS_ACTIVE, "批量恢复显示", "review.restore");
        }
    }

    @Override
    public PageResponse<AdminReportListItemVo> pageReports(long pageNum, long pageSize) {
        return PageResponse.of(List.of(), pageNum, pageSize, 0);
    }

    @Override
    public void resolveReport(Long reportId, Long adminUserId) {
        if (reportId == null) {
            throw BusinessException.badRequest("REPORT_ID_REQUIRED", "举报 ID 不能为空");
        }
        adminOperationLogService.record(
                adminUserId,
                "report_management",
                "report_resolve",
                "report",
                reportId,
                null,
                "处理举报",
                snapshot("status", "pending"),
                snapshot("status", "resolved"));
    }

    private AdminTrailListItemVo toAdminTrailListItem(TrailQueryRow row) {
        return AdminTrailListItemVo.builder()
                .id(row.getId())
                .image(row.getImage())
                .name(row.getName())
                .location(row.getLocation())
                .status(row.getStatus())
                .reviewStatus(row.getReviewStatus())
                .authorUsername(row.getAuthorUsername())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminTrailDetailVo toAdminTrailDetail(TrailQueryRow row) {
        return AdminTrailDetailVo.builder()
                .id(row.getId())
                .image(row.getImage())
                .name(row.getName())
                .location(row.getLocation())
                .geoCountry(row.getGeoCountry())
                .geoProvince(row.getGeoProvince())
                .geoCity(row.getGeoCity())
                .geoDistrict(row.getGeoDistrict())
                .geoSource(row.getGeoSource())
                .difficulty(row.getDifficulty())
                .difficultyLabel(row.getDifficultyLabel())
                .packType(row.getPackType())
                .durationType(row.getDurationType())
                .distance(row.getDistance())
                .elevation(row.getElevation())
                .duration(row.getDuration())
                .description(row.getDescription())
                .tags(splitTags(row.getTagsCsv()))
                .favorites(row.getFavorites())
                .likes(row.getLikes())
                .reviewCount(row.getReviewCount())
                .status(row.getStatus())
                .reviewStatus(row.getReviewStatus())
                .reviewRemark(row.getReviewRemark())
                .reviewedBy(row.getReviewedBy())
                .reviewedAt(row.getReviewedAt())
                .createdAt(row.getCreatedAt())
                .author(UserSummaryVo.builder()
                        .id(row.getAuthorId())
                        .username(row.getAuthorUsername())
                        .avatar(row.getAuthorAvatar())
                        .avatarBg(row.getAuthorAvatarBg())
                        .build())
                .gallery(loadGallery(row.getId()))
                .track(buildTrackVo(row.getId()))
                .build();
    }

    private Trail requireTrail(Long trailId) {
        Trail trail = trailMapper.selectById(trailId);
        if (trail == null || !"active".equals(trail.getStatus())) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return trail;
    }

    private Trail requireExistingTrail(Long trailId) {
        Trail trail = trailMapper.selectById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return trail;
    }

    private TrailQueryRow requireTrailRow(Long trailId) {
        TrailQueryRow row = trailMapper.selectAdminTrailDetailById(trailId);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return row;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }
        return user;
    }

    private Review requireReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw BusinessException.notFound("REVIEW_NOT_FOUND", "评论不存在");
        }
        return review;
    }

    private AdminReviewQueryRow requireReviewRow(Long reviewId) {
        AdminReviewQueryRow row = reviewMapper.selectAdminReviewDetailById(reviewId);
        if (row == null) {
            throw BusinessException.notFound("REVIEW_NOT_FOUND", "评论不存在");
        }
        return row;
    }

    private AdminReviewThreadItemVo toAdminReviewThreadItem(AdminReviewQueryRow row) {
        return AdminReviewThreadItemVo.builder()
                .id(row.getId())
                .userId(row.getUserId())
                .authorUsername(row.getAuthorUsername())
                .avatar(row.getAvatar())
                .avatarBg(row.getAvatarBg())
                .avatarMediaUrl(row.getAvatarMediaUrl())
                .text(row.getText())
                .status(row.getStatus())
                .moderationReason(row.getModerationReason())
                .moderatedAt(row.getModeratedAt())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private void moderateReview(Long reviewId, Long adminUserId, String targetStatus, String remark, String actionType) {
        Review review = requireReview(reviewId);
        if (targetStatus.equals(review.getStatus())) {
            throw BusinessException.badRequest("REVIEW_STATUS_UNCHANGED", "评论已处于目标状态");
        }

        reviewService.moderateReview(reviewId, adminUserId, targetStatus, remark);
        String actionCode = switch (actionType) {
            case "review.hide" -> "review_hide";
            case "review.restore" -> "review_restore";
            case "review.delete" -> "review_delete";
            default -> actionType;
        };
        adminOperationLogService.record(
                adminUserId,
                "review_management",
                actionCode,
                "review",
                reviewId,
                review.getText(),
                remark,
                snapshot("status", review.getStatus()),
                snapshot("status", targetStatus));
    }

    private String normalizeRequiredRemark(String remark, String errorMessage) {
        if (remark == null || remark.isBlank()) {
            throw BusinessException.badRequest("REVIEW_REMARK_REQUIRED", errorMessage);
        }
        return remark.trim();
    }

    private Map<String, Object> snapshot(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < keyValues.length; index += 2) {
            result.put(String.valueOf(keyValues[index]), keyValues[index + 1]);
        }
        return result;
    }

    private List<AdminDashboardTrendItemVo> buildDashboardTrends(
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime trendStartAt) {
        Map<LocalDate, Long> trailTrendMap = toDailyCountMap(trailMapper.selectDailyNewTrailCounts(trendStartAt));
        Map<LocalDate, Long> reviewTrendMap = toDailyCountMap(reviewMapper.selectDailyNewReviewCounts(trendStartAt));
        Map<LocalDate, Long> userTrendMap = toDailyCountMap(userMapper.selectDailyNewUserCounts(trendStartAt));

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> AdminDashboardTrendItemVo.builder()
                        .date(date.toString())
                        .newTrailCount(trailTrendMap.getOrDefault(date, 0L))
                        .newReviewCount(reviewTrendMap.getOrDefault(date, 0L))
                        .newReportCount(0)
                        .newUserCount(userTrendMap.getOrDefault(date, 0L))
                        .build())
                .toList();
    }

    private List<AdminDashboardRiskItemVo> buildRecentRisks() {
        return adminOperationLogMapper.selectDashboardRecentRisks(6).stream()
                .map(this::toDashboardRiskItem)
                .toList();
    }

    private AdminDashboardRiskItemVo toDashboardRiskItem(AdminDashboardRiskQueryRow row) {
        String title;
        String description;
        String type;
        String priority;
        switch (row.getActionCode()) {
            case "trail_reject" -> {
                type = "trail_rejected";
                priority = "medium";
                title = "路线审核被驳回";
                description = buildRiskDescription(row, "该路线因信息不完整被驳回。");
            }
            case "user_ban" -> {
                type = "user_banned";
                priority = "high";
                title = "用户已被封禁";
                description = buildRiskDescription(row, "管理员已执行账号封禁。");
            }
            case "review_hide" -> {
                type = "review_hidden";
                priority = "high";
                title = "评论已被隐藏";
                description = buildRiskDescription(row, "评论因存在风险内容被隐藏。");
            }
            case "report_resolve" -> {
                type = "report_resolved";
                priority = "medium";
                title = "举报已完成处理";
                description = buildRiskDescription(row, "举报已进入已处理状态。");
            }
            default -> {
                type = row.getActionCode();
                priority = "low";
                title = "后台风险事件";
                description = buildRiskDescription(row, "系统记录了一条风险动态。");
            }
        }

        return AdminDashboardRiskItemVo.builder()
                .type(type)
                .title(title)
                .description(description)
                .targetType(row.getTargetType())
                .targetId(row.getTargetId() == null ? null : String.valueOf(row.getTargetId()))
                .targetTitle(row.getTargetTitle())
                .priority(priority)
                .createdAt(row.getCreatedAt())
                .build();
    }

    private String buildRiskDescription(AdminDashboardRiskQueryRow row, String fallback) {
        if (row.getTargetTitle() != null && !row.getTargetTitle().isBlank() && row.getReason() != null && !row.getReason().isBlank()) {
            return row.getTargetTitle() + "，原因：" + row.getReason();
        }
        if (row.getReason() != null && !row.getReason().isBlank()) {
            return row.getReason();
        }
        if (row.getTargetTitle() != null && !row.getTargetTitle().isBlank()) {
            return row.getTargetTitle();
        }
        return fallback;
    }

    private Map<LocalDate, Long> toDailyCountMap(List<AdminDashboardDailyCountRow> rows) {
        return rows.stream().collect(Collectors.toMap(
                AdminDashboardDailyCountRow::getMetricDate,
                AdminDashboardDailyCountRow::getMetricCount,
                Long::sum,
                LinkedHashMap::new));
    }

    private long defaultLong(Long value) {
        return value == null ? 0 : value;
    }

    private List<String> splitTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) {
            return List.of();
        }
        return List.of(tagsCsv.split(",")).stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private List<TrailGalleryItemVo> loadGallery(Long trailId) {
        return trailImageMapper.selectByTrailId(trailId).stream()
                .map(image -> TrailGalleryItemVo.builder()
                        .mediaId(null)
                        .url(image.getImage())
                        .build())
                .toList();
    }

    private TrailTrackVo buildTrackVo(Long trailId) {
        var track = trailTrackMapper.selectByTrailId(trailId);
        if (track == null || !"parsed".equals(track.getStatus())) {
            return TrailTrackVo.builder().hasTrack(false).build();
        }

        return TrailTrackVo.builder()
                .hasTrack(true)
                .mediaFileId(track.getMediaFileId())
                .sourceFormat(track.getSourceFormat())
                .originalFileName(track.getOriginalFileName())
                .downloadUrl(null)
                .geoJson(null)
                .distanceMeters(track.getDistanceMeters())
                .elevationGainMeters(track.getElevationGainMeters())
                .elevationLossMeters(track.getElevationLossMeters())
                .durationSeconds(track.getDurationSeconds())
                .build();
    }
}
