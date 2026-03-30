package com.sheng.hikingbackend.service.impl;

import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.enums.MediaBizType;
import com.sheng.hikingbackend.common.enums.MediaFileStatus;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.enums.TrailStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.trail.CreateTrailRequest;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.dto.trail.UpdateTrailRequest;
import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.TrailTrack;
import com.sheng.hikingbackend.mapper.MediaFileMapper;
import com.sheng.hikingbackend.mapper.TagMapper;
import com.sheng.hikingbackend.mapper.TrailFavoriteMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailLikeMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTagMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.TrackParseService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.impl.support.TrackParseResult;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailGalleryItemVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;
import com.sheng.hikingbackend.vo.trail.TrailTrackVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrailServiceImpl implements TrailService {

    private static final long EDIT_WINDOW_HOURS = 48;

    private final TrailMapper trailMapper;
    private final TrailLikeMapper trailLikeMapper;
    private final TrailFavoriteMapper trailFavoriteMapper;
    private final MediaFileMapper mediaFileMapper;
    private final TrailImageMapper trailImageMapper;
    private final TrailTagMapper trailTagMapper;
    private final TagMapper tagMapper;
    private final TrailTrackMapper trailTrackMapper;
    private final GeoService geoService;
    private final TrackParseService trackParseService;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request, Long currentUserId) {
        request.setSort(normalizeSort(request.getSort()));
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> pageResult = trailMapper.selectTrailPage(page, request, currentUserId);
        List<TrailDetailVo> list = pageResult.getRecords().stream()
                .map(row -> toTrailDetailVo(row, null, currentUserId, false))
                .toList();
        return PageResponse.of(list, pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
    }

    @Override
    public TrailDetailVo getTrailDetail(Long id, Long currentUserId) {
        TrailQueryRow row = trailMapper.selectTrailDetailById(id, currentUserId);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toTrailDetailVo(row, buildTrackVo(id), currentUserId, true);
    }

    @Override
    public TrailDetailVo getTrailDetailForAdmin(Long id) {
        TrailQueryRow row = trailMapper.selectAdminTrailDetailById(id);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toTrailDetailVo(row, buildTrackVo(id), null, true);
    }

    @Override
    @Transactional
    public TrailDetailVo createTrail(Long currentUserId, String requestIp, CreateTrailRequest request) {
        TrackMutation mutation = resolveTrackMutation(currentUserId, request.getTrackMediaId());
        List<MediaFile> galleryMedia = resolveGalleryMedia(currentUserId, request.getGalleryMediaIds());
        MediaFile coverMedia = requireOwnedMedia(currentUserId, request.getCoverMediaId(), MediaBizType.TRAIL_COVER);

        Trail trail = new Trail();
        trail.setAuthorId(currentUserId);
        trail.setFavorites(0);
        trail.setLikes(0);
        trail.setRating(java.math.BigDecimal.ZERO.setScale(1));
        trail.setReviewCount(0);
        trail.setStatus(TrailStatus.ACTIVE.getCode());
        trail.setReviewStatus(TrailReviewStatus.PENDING.getCode());
        trail.setReviewRemark(null);
        trail.setReviewedBy(null);
        trail.setReviewedAt(null);
        applyTrailFields(trail, request, requestIp, coverMedia, mutation.parsedTrack());
        trailMapper.insert(trail);

        replaceGalleryImages(trail.getId(), galleryMedia);
        replaceTags(trail.getId(), request.getTags());
        replaceTrack(trail.getId(), currentUserId, mutation);

        return getTrailDetail(trail.getId(), currentUserId);
    }

    @Override
    @Transactional
    public TrailDetailVo updateTrail(Long trailId, Long currentUserId, String requestIp, UpdateTrailRequest request) {
        Trail trail = requireOwnedTrail(trailId, currentUserId);
        ensureEditable(trail);

        TrackMutation mutation = resolveTrackMutation(currentUserId, request.getTrackMediaId());
        List<MediaFile> galleryMedia = resolveGalleryMedia(currentUserId, request.getGalleryMediaIds());
        MediaFile coverMedia = requireOwnedMedia(currentUserId, request.getCoverMediaId(), MediaBizType.TRAIL_COVER);

        applyTrailFields(trail, request, requestIp, coverMedia, mutation.parsedTrack());
        trail.setReviewStatus(TrailReviewStatus.PENDING.getCode());
        trail.setReviewRemark(null);
        trail.setReviewedBy(null);
        trail.setReviewedAt(null);
        trailMapper.updateById(trail);

        replaceGalleryImages(trailId, galleryMedia);
        replaceTags(trailId, request.getTags());
        replaceTrack(trailId, currentUserId, mutation);

        return getTrailDetail(trailId, currentUserId);
    }

    @Override
    @Transactional
    public void deleteTrail(Long trailId, Long currentUserId) {
        Trail trail = requireOwnedTrail(trailId, currentUserId);
        trail.setStatus(TrailStatus.DELETED.getCode());
        trailMapper.updateById(trail);
    }

    @Override
    @Transactional
    public TrailInteractionVo likeTrail(Long trailId, Long currentUserId) {
        ensureTrailExists(trailId);
        if (trailLikeMapper.countByTrailIdAndUserId(trailId, currentUserId) == 0) {
            trailLikeMapper.insertRelation(trailId, currentUserId);
            trailMapper.incrementLikes(trailId);
        }
        return getTrailInteraction(trailId, currentUserId);
    }

    @Override
    @Transactional
    public TrailInteractionVo unlikeTrail(Long trailId, Long currentUserId) {
        ensureTrailExists(trailId);
        if (trailLikeMapper.deleteRelation(trailId, currentUserId) > 0) {
            trailMapper.decrementLikes(trailId);
        }
        return getTrailInteraction(trailId, currentUserId);
    }

    @Override
    @Transactional
    public TrailInteractionVo favoriteTrail(Long trailId, Long currentUserId) {
        ensureTrailExists(trailId);
        if (trailFavoriteMapper.countByTrailIdAndUserId(trailId, currentUserId) == 0) {
            trailFavoriteMapper.insertRelation(trailId, currentUserId);
            trailMapper.incrementFavorites(trailId);
        }
        return getTrailInteraction(trailId, currentUserId);
    }

    @Override
    @Transactional
    public TrailInteractionVo unfavoriteTrail(Long trailId, Long currentUserId) {
        ensureTrailExists(trailId);
        if (trailFavoriteMapper.deleteRelation(trailId, currentUserId) > 0) {
            trailMapper.decrementFavorites(trailId);
        }
        return getTrailInteraction(trailId, currentUserId);
    }

    @Override
    public TrailDetailVo toTrailDetailVo(TrailQueryRow row) {
        return toTrailDetailVo(row, null, null, false);
    }

    private TrailDetailVo toTrailDetailVo(
            TrailQueryRow row,
            TrailTrackVo track,
            Long currentUserId,
            boolean includeEditMedia) {
        return TrailDetailVo.builder()
                .id(row.getId())
                .image(row.getImage())
                .name(row.getName())
                .location(row.getLocation())
                .ip(row.getIp())
                .geoCountry(row.getGeoCountry())
                .geoProvince(row.getGeoProvince())
                .geoCity(row.getGeoCity())
                .geoDistrict(row.getGeoDistrict())
                .geoSource(row.getGeoSource())
                .difficulty(row.getDifficulty())
                .difficultyLabel(row.getDifficultyLabel())
                .packType(row.getPackType())
                .durationType(row.getDurationType())
                .rating(row.getRating())
                .reviewCount(row.getReviewCount())
                .distance(row.getDistance())
                .elevation(row.getElevation())
                .duration(row.getDuration())
                .description(row.getDescription())
                .tags(splitTags(row.getTagsCsv()))
                .favorites(row.getFavorites())
                .likes(row.getLikes())
                .likedByCurrentUser(Boolean.TRUE.equals(row.getLikedByCurrentUser()))
                .favoritedByCurrentUser(Boolean.TRUE.equals(row.getFavoritedByCurrentUser()))
                .authorId(row.getAuthorId())
                .publishTime(formatPublishTime(row.getCreatedAt()))
                .createdAt(row.getCreatedAt())
                .reviewStatus(row.getReviewStatus())
                .reviewRemark(row.getReviewRemark())
                .reviewedBy(row.getReviewedBy())
                .reviewedAt(row.getReviewedAt())
                .ownedByCurrentUser(isOwnedByCurrentUser(row, currentUserId))
                .editableByCurrentUser(isEditableByCurrentUser(row, currentUserId))
                .coverMediaId(includeEditMedia
                        ? resolveMediaIdByUrl(row.getAuthorId(), row.getImage(), MediaBizType.TRAIL_COVER)
                        : null)
                .gallery(includeEditMedia ? loadGallery(row.getId(), row.getAuthorId()) : List.of())
                .author(UserSummaryVo.builder()
                        .id(row.getAuthorId())
                        .username(row.getAuthorUsername())
                        .avatar(row.getAuthorAvatar())
                        .avatarBg(row.getAuthorAvatarBg())
                        .build())
                .track(track)
                .build();
    }

    private void applyTrailFields(
            Trail trail,
            CreateTrailRequest request,
            String requestIp,
            MediaFile coverMedia,
            TrackParseResult parsedTrack) {
        trail.setImage(coverMedia.getUrl());
        trail.setName(request.getName().trim());
        trail.setLocation(request.getLocation().trim());
        trail.setStartLng(parsedTrack == null ? null : parsedTrack.getStartLng());
        trail.setStartLat(parsedTrack == null ? null : parsedTrack.getStartLat());
        applyStructuredGeo(trail, request, parsedTrack);
        if (StringUtils.hasText(requestIp)) {
            trail.setIp(requestIp.trim());
        } else if (!StringUtils.hasText(trail.getIp())) {
            trail.setIp("127.0.0.1");
        }
        trail.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        trail.setDifficultyLabel(resolveDifficultyLabel(trail.getDifficulty()));
        trail.setPackType(normalizePackType(request.getPackType()));
        trail.setDurationType(normalizeDurationType(request.getDurationType()));
        trail.setDistance(resolveDistance(request.getDistance(), parsedTrack));
        trail.setElevation(resolveElevation(request.getElevation(), parsedTrack));
        trail.setDuration(resolveDuration(request.getDuration(), parsedTrack));
        trail.setDescription(normalizeOptional(request.getDescription()));
    }

    private void applyStructuredGeo(Trail trail, CreateTrailRequest request, TrackParseResult parsedTrack) {
        if (parsedTrack != null && parsedTrack.getStartLng() != null && parsedTrack.getStartLat() != null) {
            try {
                var reverseGeo = geoService.reverse(parsedTrack.getStartLng(), parsedTrack.getStartLat());
                trail.setGeoCountry(normalizeOptional(reverseGeo.getCountry()));
                trail.setGeoProvince(normalizeOptional(reverseGeo.getProvince()));
                trail.setGeoCity(normalizeOptional(reverseGeo.getCity()));
                trail.setGeoDistrict(normalizeOptional(reverseGeo.getDistrict()));
                trail.setGeoSource("track_reverse");
                return;
            } catch (BusinessException ex) {
                // Fallback to client-supplied or text lookup values below.
            }
        }

        if (hasStructuredGeo(request)) {
            trail.setGeoCountry(normalizeOptional(request.getGeoCountry()));
            trail.setGeoProvince(normalizeOptional(request.getGeoProvince()));
            trail.setGeoCity(normalizeOptional(request.getGeoCity()));
            trail.setGeoDistrict(normalizeOptional(request.getGeoDistrict()));
            trail.setGeoSource(normalizeGeoSource(request.getGeoSource(), "track_reverse"));
            return;
        }

        if (StringUtils.hasText(request.getLocation())) {
            try {
                var lookup = geoService.lookupLocation(request.getLocation().trim());
                trail.setGeoCountry(normalizeOptional(lookup.getCountry()));
                trail.setGeoProvince(normalizeOptional(lookup.getProvince()));
                trail.setGeoCity(normalizeOptional(lookup.getCity()));
                trail.setGeoDistrict(normalizeOptional(lookup.getDistrict()));
                trail.setGeoSource("location_lookup");
                return;
            } catch (BusinessException ex) {
                // Final fallback keeps visible location only.
            }
        }

        trail.setGeoCountry(null);
        trail.setGeoProvince(null);
        trail.setGeoCity(null);
        trail.setGeoDistrict(null);
        trail.setGeoSource(StringUtils.hasText(request.getLocation()) ? "manual_only" : null);
    }

    private boolean hasStructuredGeo(CreateTrailRequest request) {
        return StringUtils.hasText(request.getGeoCountry())
                || StringUtils.hasText(request.getGeoProvince())
                || StringUtils.hasText(request.getGeoCity())
                || StringUtils.hasText(request.getGeoDistrict());
    }

    private String normalizeGeoSource(String geoSource, String fallback) {
        return StringUtils.hasText(geoSource) ? geoSource.trim() : fallback;
    }

    private void replaceGalleryImages(Long trailId, List<MediaFile> galleryMedia) {
        trailImageMapper.deleteByTrailId(trailId);
        int sortOrder = 1;
        for (MediaFile mediaFile : galleryMedia) {
            trailImageMapper.insertImage(trailId, mediaFile.getUrl(), sortOrder++);
        }
    }

    private void replaceTags(Long trailId, List<String> tags) {
        trailTagMapper.deleteByTrailId(trailId);
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (String tagName : tags) {
            Long tagId = findOrCreateTagId(tagName);
            trailTagMapper.insertRelation(trailId, tagId);
        }
    }

    private void replaceTrack(Long trailId, Long currentUserId, TrackMutation mutation) {
        trailTrackMapper.deleteByTrailId(trailId);
        if (mutation.trackMedia() == null || mutation.parsedTrack() == null) {
            return;
        }

        TrailTrack trailTrack = new TrailTrack();
        trailTrack.setTrailId(trailId);
        trailTrack.setMediaFileId(mutation.trackMedia().getId());
        trailTrack.setUserId(currentUserId);
        trailTrack.setSourceFormat(mutation.parsedTrack().getSourceFormat());
        trailTrack.setOriginalFileName(mutation.trackMedia().getOriginalName());
        trailTrack.setTrackGeoJson(mutation.parsedTrack().getGeoJson());
        trailTrack.setTrackPointsCount(mutation.parsedTrack().getTrackPointsCount());
        trailTrack.setWaypointCount(mutation.parsedTrack().getWaypointCount());
        trailTrack.setStartLng(mutation.parsedTrack().getStartLng());
        trailTrack.setStartLat(mutation.parsedTrack().getStartLat());
        trailTrack.setEndLng(mutation.parsedTrack().getEndLng());
        trailTrack.setEndLat(mutation.parsedTrack().getEndLat());
        trailTrack.setBboxMinLng(mutation.parsedTrack().getBboxMinLng());
        trailTrack.setBboxMinLat(mutation.parsedTrack().getBboxMinLat());
        trailTrack.setBboxMaxLng(mutation.parsedTrack().getBboxMaxLng());
        trailTrack.setBboxMaxLat(mutation.parsedTrack().getBboxMaxLat());
        trailTrack.setDistanceMeters(mutation.parsedTrack().getDistanceMeters());
        trailTrack.setElevationMinMeters(mutation.parsedTrack().getElevationMinMeters());
        trailTrack.setElevationPeakMeters(mutation.parsedTrack().getElevationPeakMeters());
        trailTrack.setElevationGainMeters(mutation.parsedTrack().getElevationGainMeters());
        trailTrack.setElevationLossMeters(mutation.parsedTrack().getElevationLossMeters());
        trailTrack.setDurationSeconds(mutation.parsedTrack().getDurationSeconds());
        trailTrack.setStatus("parsed");
        trailTrackMapper.insert(trailTrack);
    }

    private List<MediaFile> resolveGalleryMedia(Long userId, List<Long> galleryMediaIds) {
        if (galleryMediaIds == null || galleryMediaIds.isEmpty()) {
            return Collections.emptyList();
        }
        return galleryMediaIds.stream()
                .map(mediaId -> requireOwnedMedia(userId, mediaId, MediaBizType.TRAIL_GALLERY))
                .toList();
    }

    private TrackMutation resolveTrackMutation(Long userId, Long trackMediaId) {
        if (trackMediaId == null) {
            return new TrackMutation(null, null);
        }

        MediaFile trackMedia = requireOwnedMedia(userId, trackMediaId, MediaBizType.TRAIL_TRACK);
        TrackParseResult parsedTrack = trackParseService.parse(trackMedia);
        return new TrackMutation(trackMedia, parsedTrack);
    }

    private Trail requireOwnedTrail(Long trailId, Long currentUserId) {
        Trail trail = ensureTrailExists(trailId);
        if (!Objects.equals(trail.getAuthorId(), currentUserId)) {
            throw BusinessException.forbidden("TRAIL_EDIT_FORBIDDEN", "只能编辑或删除自己发布的路线");
        }
        return trail;
    }

    private void ensureEditable(Trail trail) {
        if (!isWithinEditWindow(trail.getCreatedAt())) {
            throw BusinessException.badRequest("TRAIL_EDIT_EXPIRED", "路线发布超过48小时，不能再编辑");
        }
    }

    private boolean isOwnedByCurrentUser(TrailQueryRow row, Long currentUserId) {
        return currentUserId != null && Objects.equals(row.getAuthorId(), currentUserId);
    }

    private boolean isEditableByCurrentUser(TrailQueryRow row, Long currentUserId) {
        return isOwnedByCurrentUser(row, currentUserId) && isWithinEditWindow(row.getCreatedAt());
    }

    private boolean isWithinEditWindow(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        return !createdAt.isBefore(LocalDateTime.now().minusHours(EDIT_WINDOW_HOURS));
    }

    private List<TrailGalleryItemVo> loadGallery(Long trailId, Long authorId) {
        return trailImageMapper.selectByTrailId(trailId).stream()
                .map(image -> TrailGalleryItemVo.builder()
                        .mediaId(resolveMediaIdByUrl(authorId, image.getImage(), MediaBizType.TRAIL_GALLERY))
                        .url(image.getImage())
                        .build())
                .toList();
    }

    private Long resolveMediaIdByUrl(Long userId, String url, MediaBizType bizType) {
        if (userId == null || !StringUtils.hasText(url)) {
            return null;
        }

        MediaFile mediaFile = mediaFileMapper.selectOne(new LambdaQueryWrapper<MediaFile>()
                .eq(MediaFile::getUserId, userId)
                .eq(MediaFile::getUrl, url)
                .eq(MediaFile::getBizType, bizType.getCode())
                .eq(MediaFile::getStatus, MediaFileStatus.ACTIVE.getCode())
                .last("LIMIT 1"));
        return mediaFile == null ? null : mediaFile.getId();
    }

    private Trail ensureTrailExists(Long trailId) {
        Trail trail = trailMapper.selectActiveById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return trail;
    }

    private TrailInteractionVo getTrailInteraction(Long trailId, Long currentUserId) {
        TrailInteractionVo interaction = trailMapper.selectTrailInteraction(trailId, currentUserId);
        if (interaction == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        interaction.setLikedByCurrentUser(Boolean.TRUE.equals(interaction.getLikedByCurrentUser()));
        interaction.setFavoritedByCurrentUser(Boolean.TRUE.equals(interaction.getFavoritedByCurrentUser()));
        return interaction;
    }

    private List<String> splitTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagsCsv.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    @Override
    public String formatPublishTime(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = Math.max(duration.toMinutes(), 0);
        if (minutes < 60) {
            return minutes <= 1 ? "刚刚" : minutes + " 分钟前";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }

        long days = duration.toDays();
        if (days < 30) {
            return days + " 天前";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " 个月前";
        }

        long years = months / 12;
        return years + " 年前";
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "latest";
        }

        return switch (sort) {
            case "hot", "latest", "rating" -> sort;
            default -> "latest";
        };
    }

    private MediaFile requireOwnedMedia(Long userId, Long mediaId, MediaBizType bizType) {
        MediaFile mediaFile = mediaFileMapper.selectOne(new LambdaQueryWrapper<MediaFile>()
                .eq(MediaFile::getId, mediaId)
                .eq(MediaFile::getUserId, userId)
                .eq(MediaFile::getBizType, bizType.getCode())
                .eq(MediaFile::getStatus, MediaFileStatus.ACTIVE.getCode())
                .last("LIMIT 1"));
        if (mediaFile == null) {
            throw BusinessException.badRequest("INVALID_MEDIA_FILE", "上传文件不存在或无权使用");
        }
        return mediaFile;
    }

    private Long findOrCreateTagId(String tagName) {
        String normalized = tagName == null ? "" : tagName.trim();
        if (normalized.isBlank()) {
            throw BusinessException.badRequest("INVALID_TAG", "标签不能为空");
        }

        Long existingId = tagMapper.selectIdByName(normalized);
        if (existingId != null) {
            return existingId;
        }

        try {
            tagMapper.insertTag(normalized);
        } catch (DuplicateKeyException ignored) {
        }

        Long createdId = tagMapper.selectIdByName(normalized);
        if (createdId == null) {
            throw BusinessException.badRequest("TAG_CREATE_FAILED", "标签创建失败");
        }
        return createdId;
    }

    private String normalizeDifficulty(String difficulty) {
        return switch (difficulty) {
            case "easy", "moderate", "hard" -> difficulty;
            default -> throw BusinessException.badRequest("INVALID_DIFFICULTY", "难度选项不合法");
        };
    }

    private String resolveDifficultyLabel(String difficulty) {
        return switch (difficulty) {
            case "easy" -> "简单";
            case "moderate" -> "适中";
            case "hard" -> "困难";
            default -> "适中";
        };
    }

    private String normalizePackType(String packType) {
        return switch (packType) {
            case "light", "heavy", "both" -> packType;
            default -> throw BusinessException.badRequest("INVALID_PACK_TYPE", "负重类型不合法");
        };
    }

    private String normalizeDurationType(String durationType) {
        return switch (durationType) {
            case "single_day", "multi_day" -> durationType;
            default -> throw BusinessException.badRequest("INVALID_DURATION_TYPE", "行程类型不合法");
        };
    }

    private String resolveDistance(String manualValue, TrackParseResult parsedTrack) {
        String normalized = normalizeOptional(manualValue);
        if (normalized != null) {
            return normalized;
        }
        if (parsedTrack != null && parsedTrack.getDistanceMeters() != null) {
            return formatDistance(parsedTrack.getDistanceMeters());
        }
        return null;
    }

    private String resolveElevation(String manualValue, TrackParseResult parsedTrack) {
        String normalized = normalizeOptional(manualValue);
        if (normalized != null) {
            return normalized;
        }
        if (parsedTrack != null && parsedTrack.getElevationGainMeters() != null) {
            return "+" + parsedTrack.getElevationGainMeters().setScale(0, RoundingMode.HALF_UP).toPlainString() + " m";
        }
        return null;
    }

    private String resolveDuration(String manualValue, TrackParseResult parsedTrack) {
        String normalized = normalizeOptional(manualValue);
        if (normalized != null) {
            return normalized;
        }
        if (parsedTrack != null && parsedTrack.getDurationSeconds() != null && parsedTrack.getDurationSeconds() > 0) {
            return formatDurationSeconds(parsedTrack.getDurationSeconds());
        }
        return null;
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String formatDistance(java.math.BigDecimal distanceMeters) {
        return distanceMeters.divide(java.math.BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + " km";
    }

    private String formatDurationSeconds(Long seconds) {
        long totalMinutes = Math.max(seconds / 60, 1);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        if (hours == 0) {
            return minutes + "m";
        }
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }

    private TrailTrackVo buildTrackVo(Long trailId) {
        TrailTrack track = trailTrackMapper.selectByTrailId(trailId);
        if (track == null || !"parsed".equals(track.getStatus())) {
            return TrailTrackVo.builder().hasTrack(false).build();
        }

        MediaFile mediaFile = mediaFileMapper.selectById(track.getMediaFileId());
        JsonNode geoJson = null;
        try {
            geoJson = objectMapper.readTree(track.getTrackGeoJson());
        } catch (Exception ignored) {
            geoJson = null;
        }

        return TrailTrackVo.builder()
                .hasTrack(true)
                .mediaFileId(track.getMediaFileId())
                .sourceFormat(track.getSourceFormat())
                .originalFileName(track.getOriginalFileName())
                .downloadUrl(mediaFile == null ? null : mediaFile.getUrl())
                .geoJson(geoJson)
                .startPoint(TrailTrackVo.TrackPointVo.builder()
                        .lng(track.getStartLng())
                        .lat(track.getStartLat())
                        .build())
                .endPoint(TrailTrackVo.TrackPointVo.builder()
                        .lng(track.getEndLng())
                        .lat(track.getEndLat())
                        .build())
                .bounds(TrailTrackVo.TrackBoundsVo.builder()
                        .minLng(track.getBboxMinLng())
                        .minLat(track.getBboxMinLat())
                        .maxLng(track.getBboxMaxLng())
                        .maxLat(track.getBboxMaxLat())
                        .build())
                .distanceMeters(track.getDistanceMeters())
                .elevationMinMeters(track.getElevationMinMeters())
                .elevationPeakMeters(track.getElevationPeakMeters())
                .elevationGainMeters(track.getElevationGainMeters())
                .elevationLossMeters(track.getElevationLossMeters())
                .durationSeconds(track.getDurationSeconds())
                .build();
    }

    private record TrackMutation(MediaFile trackMedia, TrackParseResult parsedTrack) {
    }
}
