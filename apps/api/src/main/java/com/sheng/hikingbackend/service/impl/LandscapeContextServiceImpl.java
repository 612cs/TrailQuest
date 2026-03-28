package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.TrailTrack;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.LandscapeContextService;
import com.sheng.hikingbackend.service.landscape.LandscapeAstronomyService;
import com.sheng.hikingbackend.service.landscape.LandscapeMeteorologyService;
import com.sheng.hikingbackend.service.landscape.LightPollutionService;
import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.service.landscape.model.LightPollutionContext;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;

@Service
public class LandscapeContextServiceImpl implements LandscapeContextService {

    private final TrailMapper trailMapper;
    private final TrailTrackMapper trailTrackMapper;
    private final GeoService geoService;
    private final LandscapeMeteorologyService meteorologyService;
    private final LandscapeAstronomyService astronomyService;
    private final LightPollutionService lightPollutionService;
    private final ObjectMapper objectMapper;

    public LandscapeContextServiceImpl(
            TrailMapper trailMapper,
            TrailTrackMapper trailTrackMapper,
            GeoService geoService,
            LandscapeMeteorologyService meteorologyService,
            LandscapeAstronomyService astronomyService,
            LightPollutionService lightPollutionService,
            ObjectMapper objectMapper) {
        this.trailMapper = trailMapper;
        this.trailTrackMapper = trailTrackMapper;
        this.geoService = geoService;
        this.meteorologyService = meteorologyService;
        this.astronomyService = astronomyService;
        this.lightPollutionService = lightPollutionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public LandscapeContext build(Long trailId, int days) {
        Trail trail = trailMapper.selectActiveById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在或已删除");
        }

        TrailTrack track = trailTrackMapper.selectByTrailId(trailId);
        ResolvedLocation location = resolveLocation(trail);
        BigDecimal peakElevation = resolvePeakElevation(track);
        BigDecimal minElevation = resolveMinElevation(track);
        CurrentWeatherSnapshot currentWeather = null;
        List<HourlyWeatherPoint> hourlyForecast = Collections.emptyList();
        boolean weatherReady = false;
        try {
            currentWeather = meteorologyService.getCurrentWeather(location.lng(), location.lat());
            hourlyForecast = meteorologyService.getHourlyForecast(location.lng(), location.lat(), Math.min(Math.max(days, 1) * 24, 168));
            weatherReady = currentWeather != null && !hourlyForecast.isEmpty();
        } catch (BusinessException ex) {
            weatherReady = false;
        }
        List<SunAstronomyPoint> sunForecast = new ArrayList<>();
        List<MoonAstronomyPoint> moonForecast = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            try {
                sunForecast.add(astronomyService.getSunInfo(location.lng(), location.lat(), date, peakElevation));
            } catch (BusinessException ex) {
                // degrade gracefully when a single day of astronomy data is unavailable
            }
            try {
                moonForecast.add(astronomyService.getMoonInfo(location.lng(), location.lat(), date));
            } catch (BusinessException ex) {
                // degrade gracefully when moon phase data is unavailable
            }
        }
        LightPollutionContext lightPollution = lightPollutionService.resolve(location.lng(), location.lat(), trail.getLocation());
        boolean astroReady = !sunForecast.isEmpty() && !moonForecast.isEmpty();

        return LandscapeContext.builder()
                .trailId(trail.getId())
                .trailName(trail.getName())
                .location(trail.getLocation())
                .lng(location.lng())
                .lat(location.lat())
                .resolvedFrom(location.resolvedFrom())
                .elevationGainMeters(track == null ? null : track.getElevationGainMeters())
                .elevationPeakMeters(peakElevation)
                .elevationMinMeters(minElevation)
                .currentWeather(currentWeather)
                .hourlyForecast(hourlyForecast)
                .sunForecast(sunForecast)
                .moonForecast(moonForecast)
                .lightPollution(lightPollution)
                .weatherReady(weatherReady)
                .astroReady(astroReady)
                .days(days)
                .build();
    }

    private ResolvedLocation resolveLocation(Trail trail) {
        if (trail.getStartLng() != null && trail.getStartLat() != null) {
            return new ResolvedLocation(trail.getStartLng(), trail.getStartLat(), "start_coordinate");
        }
        if (StringUtils.hasText(trail.getLocation())) {
            GeoLookupResponse geo = geoService.lookupLocation(trail.getLocation());
            return new ResolvedLocation(geo.getLng(), geo.getLat(), "location_text");
        }
        throw BusinessException.badRequest("LANDSCAPE_LOCATION_UNAVAILABLE", "路线缺少可用的位置数据，暂时无法进行景观预测");
    }

    private BigDecimal resolvePeakElevation(TrailTrack track) {
        if (track == null) {
            return null;
        }
        if (track.getElevationPeakMeters() != null) {
            return track.getElevationPeakMeters();
        }
        return extractElevation(track.getTrackGeoJson(), true);
    }

    private BigDecimal resolveMinElevation(TrailTrack track) {
        if (track == null) {
            return null;
        }
        if (track.getElevationMinMeters() != null) {
            return track.getElevationMinMeters();
        }
        return extractElevation(track.getTrackGeoJson(), false);
    }

    private BigDecimal extractElevation(String geoJson, boolean max) {
        if (!StringUtils.hasText(geoJson)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode features = root.path("features");
            BigDecimal result = null;
            for (JsonNode feature : features) {
                JsonNode geometry = feature.path("geometry");
                JsonNode coordinates = geometry.path("coordinates");
                result = walkCoordinates(coordinates, result, max);
            }
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal walkCoordinates(JsonNode node, BigDecimal current, boolean max) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return current;
        }
        if (node.isArray() && node.size() >= 3 && node.get(0).isNumber() && node.get(1).isNumber() && node.get(2).isNumber()) {
            BigDecimal value = node.get(2).decimalValue();
            if (current == null) {
                return value;
            }
            return max ? (value.compareTo(current) > 0 ? value : current) : (value.compareTo(current) < 0 ? value : current);
        }
        if (node.isArray()) {
            BigDecimal result = current;
            for (JsonNode child : node) {
                result = walkCoordinates(child, result, max);
            }
            return result;
        }
        return current;
    }

    private record ResolvedLocation(BigDecimal lng, BigDecimal lat, String resolvedFrom) {
    }
}
