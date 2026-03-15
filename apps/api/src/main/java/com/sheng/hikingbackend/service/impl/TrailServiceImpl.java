package com.sheng.hikingbackend.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrailServiceImpl implements TrailService {

    private final TrailMapper trailMapper;

    @Override
    public PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request) {
        Page<TrailQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<TrailQueryRow> pageResult = trailMapper.selectTrailPage(page, request);
        List<TrailDetailVo> list = pageResult.getRecords().stream()
                .map(this::toTrailDetailVo)
                .toList();
        return PageResponse.of(list, pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
    }

    @Override
    public TrailDetailVo getTrailDetail(Long id) {
        TrailQueryRow row = trailMapper.selectTrailDetailById(id);
        if (row == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return toTrailDetailVo(row);
    }

    private TrailDetailVo toTrailDetailVo(TrailQueryRow row) {
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

    private List<String> splitTags(String tagsCsv) {
        if (tagsCsv == null || tagsCsv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagsCsv.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    private String formatPublishTime(LocalDateTime createdAt) {
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
}
