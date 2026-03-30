package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminRejectTrailRequest;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.ReviewMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.service.AdminService;
import com.sheng.hikingbackend.service.ReviewService;
import com.sheng.hikingbackend.vo.admin.AdminDashboardSummaryVo;
import com.sheng.hikingbackend.vo.admin.AdminReportListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminReviewQueryRow;
import com.sheng.hikingbackend.vo.admin.AdminTrailDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminTrailListItemVo;
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

    @Override
    public AdminDashboardSummaryVo getDashboardSummary() {
        return AdminDashboardSummaryVo.builder()
                .pendingTrailCount(Math.toIntExact(trailMapper.countPendingReviewTrails()))
                .reviewCount(Math.toIntExact(reviewMapper.countAllReviews()))
                .pendingReportCount(0)
                .build();
    }

    @Override
    public PageResponse<AdminTrailListItemVo> pageTrails(AdminTrailPageRequest request) {
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> result = trailMapper.selectAdminTrailPage(page, request);
        List<AdminTrailListItemVo> list = result.getRecords().stream()
                .map(row -> AdminTrailListItemVo.builder()
                        .id(row.getId())
                        .image(row.getImage())
                        .name(row.getName())
                        .location(row.getLocation())
                        .reviewStatus(row.getReviewStatus())
                        .authorUsername(row.getAuthorUsername())
                        .createdAt(row.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.of(list, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public AdminTrailDetailVo getTrailDetail(Long trailId) {
        TrailQueryRow row = trailMapper.selectAdminTrailDetailById(trailId);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }

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

    @Override
    @Transactional
    public void approveTrail(Long trailId, Long adminUserId) {
        Trail trail = requireTrail(trailId);
        trail.setReviewStatus(TrailReviewStatus.APPROVED.getCode());
        trail.setReviewRemark(null);
        trail.setReviewedBy(adminUserId);
        trail.setReviewedAt(LocalDateTime.now());
        trailMapper.updateById(trail);
    }

    @Override
    @Transactional
    public void rejectTrail(Long trailId, Long adminUserId, AdminRejectTrailRequest request) {
        Trail trail = requireTrail(trailId);
        trail.setReviewStatus(TrailReviewStatus.REJECTED.getCode());
        trail.setReviewRemark(request.getRemark().trim());
        trail.setReviewedBy(adminUserId);
        trail.setReviewedAt(LocalDateTime.now());
        trailMapper.updateById(trail);
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
        // 第一版后台先保留治理入口，等前台举报链路接入后再补真实处理逻辑。
        if (reportId == null) {
            throw BusinessException.badRequest("REPORT_ID_REQUIRED", "举报 ID 不能为空");
        }
    }

    private Trail requireTrail(Long trailId) {
        Trail trail = trailMapper.selectById(trailId);
        if (trail == null || !"active".equals(trail.getStatus())) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return trail;
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
