package com.sheng.hikingbackend.service.impl;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sheng.hikingbackend.config.TrailGeoBackfillProperties;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrailGeoBackfillRunner implements ApplicationRunner {

    private final TrailMapper trailMapper;
    private final GeoService geoService;
    private final TrailGeoBackfillProperties trailGeoBackfillProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!trailGeoBackfillProperties.isEnabled()) {
            return;
        }

        List<Trail> trails = trailMapper.selectList(new LambdaQueryWrapper<Trail>()
                .eq(Trail::getStatus, "active")
                .and(wrapper -> wrapper
                        .isNull(Trail::getGeoProvince)
                        .or()
                        .isNull(Trail::getGeoCity)
                        .or()
                        .isNull(Trail::getGeoDistrict)));

        int successCount = 0;
        for (Trail trail : trails) {
            boolean updated = backfillTrail(trail);
            if (updated) {
                trailMapper.updateById(trail);
                successCount += 1;
            }
        }

        log.info("Trail geo backfill finished total={} success={}", trails.size(), successCount);
    }

    private boolean backfillTrail(Trail trail) {
        try {
            if (trail.getStartLng() != null && trail.getStartLat() != null) {
                var reverseGeo = geoService.reverse(trail.getStartLng(), trail.getStartLat());
                trail.setGeoCountry(reverseGeo.getCountry());
                trail.setGeoProvince(reverseGeo.getProvince());
                trail.setGeoCity(reverseGeo.getCity());
                trail.setGeoDistrict(reverseGeo.getDistrict());
                trail.setGeoSource("backfill");
                return true;
            }

            if (StringUtils.hasText(trail.getLocation())) {
                var lookup = geoService.lookupLocation(trail.getLocation());
                trail.setGeoCountry(lookup.getCountry());
                trail.setGeoProvince(lookup.getProvince());
                trail.setGeoCity(lookup.getCity());
                trail.setGeoDistrict(lookup.getDistrict());
                trail.setGeoSource("backfill");
                return true;
            }
        } catch (Exception ex) {
            log.warn("Trail geo backfill skipped trailId={} reason={}", trail.getId(), ex.getMessage());
        }
        return false;
    }
}
