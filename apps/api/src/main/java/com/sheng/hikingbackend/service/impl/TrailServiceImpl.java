package com.sheng.hikingbackend.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.mapper.TrailFavoriteMapper;
import com.sheng.hikingbackend.mapper.TrailLikeMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrailServiceImpl implements TrailService {

    private final TrailMapper trailMapper;
    private final TrailLikeMapper trailLikeMapper;
    private final TrailFavoriteMapper trailFavoriteMapper;

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
        return toTrailDetailVo(row);
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
}
