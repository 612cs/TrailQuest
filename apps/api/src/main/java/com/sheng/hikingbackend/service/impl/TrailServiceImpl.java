package com.sheng.hikingbackend.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.trail.CreateTrailRequest;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
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
import com.sheng.hikingbackend.service.TrackParseService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.impl.support.TrackParseResult;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;
import com.sheng.hikingbackend.vo.trail.TrailTrackVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrailServiceImpl implements TrailService {

    private final TrailMapper trailMapper;
    private final TrailLikeMapper trailLikeMapper;
    private final TrailFavoriteMapper trailFavoriteMapper;
    private final MediaFileMapper mediaFileMapper;
    private final TrailImageMapper trailImageMapper;
    private final TrailTagMapper trailTagMapper;
    private final TagMapper tagMapper;
    private final TrailTrackMapper trailTrackMapper;
    private final TrackParseService trackParseService;
    private final ObjectMapper objectMapper;

    @Override
    public PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request, Long currentUserId) {
        request.setSort(normalizeSort(request.getSort()));
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> pageResult = trailMapper.selectTrailPage(page, request, currentUserId);
        List<TrailDetailVo> list = pageResult.getRecords().stream()
                .map(this::toTrailDetailVo)
                .toList();
        return PageResponse.of(list, pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
    }

    @Override
    public TrailDetailVo getTrailDetail(Long id, Long currentUserId) {
        TrailQueryRow row = trailMapper.selectTrailDetailById(id, currentUserId);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toTrailDetailVo(row, buildTrackVo(id));
    }

    @Override
    @Transactional
    public TrailDetailVo createTrail(Long currentUserId, String requestIp, CreateTrailRequest request) {
        MediaFile coverMedia = requireOwnedMedia(currentUserId, request.getCoverMediaId(), MediaBizType.TRAIL_COVER);
        List<MediaFile> galleryMedia = request.getGalleryMediaIds() == null
                ? Collections.emptyList()
                : request.getGalleryMediaIds().stream()
                        .map(mediaId -> requireOwnedMedia(currentUserId, mediaId, MediaBizType.TRAIL_GALLERY))
                        .toList();

        TrackParseResult parsedTrack = null;
        MediaFile trackMedia = null;
        if (request.getTrackMediaId() != null) {
            trackMedia = requireOwnedMedia(currentUserId, request.getTrackMediaId(), MediaBizType.TRAIL_TRACK);
            parsedTrack = trackParseService.parse(trackMedia);
        }

        Trail trail = new Trail();
        trail.setImage(coverMedia.getUrl());
        trail.setName(request.getName().trim());
        trail.setLocation(request.getLocation().trim());
        trail.setIp(StringUtils.hasText(requestIp) ? requestIp.trim() : "127.0.0.1");
        trail.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        trail.setDifficultyLabel(resolveDifficultyLabel(trail.getDifficulty()));
        trail.setPackType(normalizePackType(request.getPackType()));
        trail.setDurationType(normalizeDurationType(request.getDurationType()));
        trail.setDistance(resolveDistance(request.getDistance(), parsedTrack));
        trail.setElevation(resolveElevation(request.getElevation(), parsedTrack));
        trail.setDuration(resolveDuration(request.getDuration(), parsedTrack));
        trail.setDescription(request.getDescription().trim());
        trail.setFavorites(0);
        trail.setLikes(0);
        trail.setRating(java.math.BigDecimal.ZERO.setScale(1));
        trail.setReviewCount(0);
        trail.setAuthorId(currentUserId);
        trailMapper.insert(trail);

        int sortOrder = 1;
        for (MediaFile mediaFile : galleryMedia) {
            trailImageMapper.insertImage(trail.getId(), mediaFile.getUrl(), sortOrder++);
        }

        for (String tagName : request.getTags()) {
            Long tagId = findOrCreateTagId(tagName);
            trailTagMapper.insertRelation(trail.getId(), tagId);
        }

        if (trackMedia != null && parsedTrack != null) {
            TrailTrack trailTrack = new TrailTrack();
            trailTrack.setTrailId(trail.getId());
            trailTrack.setMediaFileId(trackMedia.getId());
            trailTrack.setUserId(currentUserId);
            trailTrack.setSourceFormat(parsedTrack.getSourceFormat());
            trailTrack.setOriginalFileName(trackMedia.getOriginalName());
            trailTrack.setTrackGeoJson(parsedTrack.getGeoJson());
            trailTrack.setTrackPointsCount(parsedTrack.getTrackPointsCount());
            trailTrack.setWaypointCount(parsedTrack.getWaypointCount());
            trailTrack.setStartLng(parsedTrack.getStartLng());
            trailTrack.setStartLat(parsedTrack.getStartLat());
            trailTrack.setEndLng(parsedTrack.getEndLng());
            trailTrack.setEndLat(parsedTrack.getEndLat());
            trailTrack.setBboxMinLng(parsedTrack.getBboxMinLng());
            trailTrack.setBboxMinLat(parsedTrack.getBboxMinLat());
            trailTrack.setBboxMaxLng(parsedTrack.getBboxMaxLng());
            trailTrack.setBboxMaxLat(parsedTrack.getBboxMaxLat());
            trailTrack.setDistanceMeters(parsedTrack.getDistanceMeters());
            trailTrack.setElevationGainMeters(parsedTrack.getElevationGainMeters());
            trailTrack.setElevationLossMeters(parsedTrack.getElevationLossMeters());
            trailTrack.setDurationSeconds(parsedTrack.getDurationSeconds());
            trailTrack.setStatus("parsed");
            trailTrackMapper.insert(trailTrack);
        }

        TrailQueryRow created = trailMapper.selectTrailDetailById(trail.getId(), currentUserId);
        if (created == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toTrailDetailVo(created, buildTrackVo(trail.getId()));
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
        return toTrailDetailVo(row, null);
    }

    private TrailDetailVo toTrailDetailVo(TrailQueryRow row, TrailTrackVo track) {
        return TrailDetailVo.builder()
                .id(row.getId())
                .image(row.getImage())
                .name(row.getName())
                .location(row.getLocation())
                .ip(row.getIp())
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
                .author(UserSummaryVo.builder()
                        .id(row.getAuthorId())
                        .username(row.getAuthorUsername())
                        .avatar(row.getAuthorAvatar())
                        .avatarBg(row.getAuthorAvatarBg())
                        .build())
                .track(track)
                .build();
    }

    private void ensureTrailExists(Long trailId) {
        if (trailMapper.selectById(trailId) == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
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
        throw BusinessException.badRequest("DISTANCE_REQUIRED", "请填写路线距离或上传轨迹自动补全");
    }

    private String resolveElevation(String manualValue, TrackParseResult parsedTrack) {
        String normalized = normalizeOptional(manualValue);
        if (normalized != null) {
            return normalized;
        }
        if (parsedTrack != null && parsedTrack.getElevationGainMeters() != null) {
            return "+" + parsedTrack.getElevationGainMeters().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + " m";
        }
        throw BusinessException.badRequest("ELEVATION_REQUIRED", "请填写海拔爬升或上传轨迹自动补全");
    }

    private String resolveDuration(String manualValue, TrackParseResult parsedTrack) {
        String normalized = normalizeOptional(manualValue);
        if (normalized != null) {
            return normalized;
        }
        if (parsedTrack != null && parsedTrack.getDurationSeconds() != null && parsedTrack.getDurationSeconds() > 0) {
            return formatDurationSeconds(parsedTrack.getDurationSeconds());
        }
        throw BusinessException.badRequest("DURATION_REQUIRED", "请填写预计时长或上传包含时间信息的轨迹");
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String formatDistance(java.math.BigDecimal distanceMeters) {
        return distanceMeters.divide(java.math.BigDecimal.valueOf(1000), 1, java.math.RoundingMode.HALF_UP)
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
                .elevationGainMeters(track.getElevationGainMeters())
                .elevationLossMeters(track.getElevationLossMeters())
                .durationSeconds(track.getDurationSeconds())
                .build();
    }
}
