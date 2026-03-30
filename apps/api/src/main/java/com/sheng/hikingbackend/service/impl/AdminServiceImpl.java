package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.enums.UserStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminBanUserRequest;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailManagementPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.entity.AdminOperationLog;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.AdminOperationLogMapper;
import com.sheng.hikingbackend.mapper.ReviewMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AdminService;
import com.sheng.hikingbackend.service.ReviewService;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewQueryRow;
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

    private final TrailMapper trailMapper;
    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;
    private final TrailImageMapper trailImageMapper;
    private final TrailTrackMapper trailTrackMapper;
    private final UserMapper userMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AdminDashboardSummaryVo getDashboardSummary() {
        return AdminDashboardSummaryVo.builder()
                .pendingTrailCount(Math.toIntExact(trailMapper.countPendingReviewTrails()))
                .reviewCount(Math.toIntExact(reviewMapper.countAllReviews()))
                .pendingReportCount(0)
                .userCount(Math.toIntExact(userMapper.countAllUsers()))
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

        logAction("user.ban", "user", userId, adminUserId, request.getReason(), Map.of(
                "status", user.getStatus()));
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

        logAction("user.unban", "user", userId, adminUserId, "恢复账号", Map.of(
                "status", user.getStatus()));
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

        logAction("trail.approve", "trail", trailId, adminUserId, "审核通过", Map.of(
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

        logAction("trail.reject", "trail", trailId, adminUserId, remark, Map.of(
                "status", trail.getStatus(),
                "reviewStatus", trail.getReviewStatus()));
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

        logAction("trail.offline", "trail", trailId, adminUserId, "路线下架", Map.of(
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

        logAction("trail.restore", "trail", trailId, adminUserId, "路线恢复", Map.of(
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
                        .text(row.getText())
                        .authorUsername(row.getAuthorUsername())
                        .trailName(row.getTrailName())
                        .createdAt(row.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewService.deleteReviewAsAdmin(reviewId);
    }

    @Override
    public PageResponse<AdminReportListItemVo> pageReports(long pageNum, long pageSize) {
        return PageResponse.of(List.of(), pageNum, pageSize, 0);
    }

    @Override
    public void resolveReport(Long reportId) {
        if (reportId == null) {
            throw BusinessException.badRequest("REPORT_ID_REQUIRED", "举报 ID 不能为空");
        }
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

    private void logAction(
            String actionType,
            String targetType,
            Long targetId,
            Long operatorId,
            String remark,
            Map<String, Object> metadata) {
        AdminOperationLog log = new AdminOperationLog();
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setMetadataJson(writeMetadata(metadata));
        log.setCreatedAt(LocalDateTime.now());
        adminOperationLogMapper.insert(log);
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize admin operation metadata", ex);
        }
    }
}
