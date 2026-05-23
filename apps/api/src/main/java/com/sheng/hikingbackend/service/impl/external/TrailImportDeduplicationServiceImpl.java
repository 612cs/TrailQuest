package com.sheng.hikingbackend.service.impl.external;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.external.TrailImportDeduplicationService;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.TrailDeduplicationMatch;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrailImportDeduplicationServiceImpl implements TrailImportDeduplicationService {

    private final TrailMapper trailMapper;

    @Override
    public TrailDeduplicationMatch match(ExternalTrailCandidate candidate) {
        Trail sourceUrlMatch = matchBySourceUrl(candidate);
        if (sourceUrlMatch != null) {
            return toMatch(sourceUrlMatch, "source_url");
        }

        Trail geoMatch = matchByNameAndGeo(candidate);
        if (geoMatch != null) {
            return toMatch(geoMatch, "name_geo");
        }

        Trail locationMatch = matchByNameAndLocation(candidate);
        if (locationMatch != null) {
            return toMatch(locationMatch, "name_location");
        }

        return TrailDeduplicationMatch.notMatched();
    }

    private Trail matchBySourceUrl(ExternalTrailCandidate candidate) {
        if (!StringUtils.hasText(candidate.getSourceUrl())) {
            return null;
        }
        List<Trail> trails = trailMapper.selectList(new LambdaQueryWrapper<Trail>()
                .eq(Trail::getSourceUrl, candidate.getSourceUrl())
                .last("LIMIT 1"));
        return trails.isEmpty() ? null : trails.get(0);
    }

    private Trail matchByNameAndGeo(ExternalTrailCandidate candidate) {
        if (!StringUtils.hasText(candidate.getName())) {
            return null;
        }
        if (!StringUtils.hasText(candidate.getGeoCity()) && !StringUtils.hasText(candidate.getGeoDistrict())) {
            return null;
        }

        LambdaQueryWrapper<Trail> queryWrapper = new LambdaQueryWrapper<Trail>()
                .eq(Trail::getName, candidate.getName())
                .last("LIMIT 1");
        if (StringUtils.hasText(candidate.getGeoDistrict())) {
            queryWrapper.eq(Trail::getGeoDistrict, candidate.getGeoDistrict());
        } else {
            queryWrapper.eq(Trail::getGeoCity, candidate.getGeoCity());
        }
        List<Trail> trails = trailMapper.selectList(queryWrapper);
        return trails.isEmpty() ? null : trails.get(0);
    }

    private Trail matchByNameAndLocation(ExternalTrailCandidate candidate) {
        if (!StringUtils.hasText(candidate.getName()) || !StringUtils.hasText(candidate.getLocation())) {
            return null;
        }
        List<Trail> trails = trailMapper.selectList(new LambdaQueryWrapper<Trail>()
                .eq(Trail::getName, candidate.getName())
                .eq(Trail::getLocation, candidate.getLocation())
                .last("LIMIT 1"));
        return trails.isEmpty() ? null : trails.get(0);
    }

    private TrailDeduplicationMatch toMatch(Trail trail, String matchedBy) {
        return TrailDeduplicationMatch.builder()
                .duplicate(true)
                .trailId(trail.getId())
                .matchedBy(matchedBy)
                .reviewStatus(trail.getReviewStatus())
                .aiReviewStatus(trail.getAiReviewStatus())
                .build();
    }
}
