package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.TrailWeatherService;
import com.sheng.hikingbackend.service.WeatherService;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;
import com.sheng.hikingbackend.vo.weather.TrailWeatherForecastDayVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherLocationContextVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherResponseVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherSourceVo;

@Service
public class TrailWeatherServiceImpl implements TrailWeatherService {

    private static final String RESOLVED_FROM_START_COORDINATE = "start_coordinate";
    private static final String RESOLVED_FROM_LOCATION_TEXT = "location_text";

    private final TrailMapper trailMapper;
    private final GeoService geoService;
    private final WeatherService weatherService;

    public TrailWeatherServiceImpl(
            TrailMapper trailMapper,
            GeoService geoService,
            WeatherService weatherService) {
        this.trailMapper = trailMapper;
        this.geoService = geoService;
        this.weatherService = weatherService;
    }

    @Override
    public TrailWeatherResponseVo getTrailWeather(Long trailId) {
        Trail trail = trailMapper.selectActiveById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在或已删除");
        }

        ResolvedTrailLocation resolvedLocation = resolveLocation(trail);
        List<TrailWeatherForecastDayVo> forecast = weatherService.getSevenDayForecast(
                resolvedLocation.lng(),
                resolvedLocation.lat());

        return TrailWeatherResponseVo.builder()
                .locationContext(TrailWeatherLocationContextVo.builder()
                        .lng(resolvedLocation.lng())
                        .lat(resolvedLocation.lat())
                        .resolvedFrom(resolvedLocation.resolvedFrom())
                        .build())
                .forecast(forecast)
                .source(TrailWeatherSourceVo.builder()
                        .provider("qweather")
                        .cached(Boolean.FALSE)
                        .build())
                .build();
    }

    private ResolvedTrailLocation resolveLocation(Trail trail) {
        if (trail.getStartLng() != null && trail.getStartLat() != null) {
            return new ResolvedTrailLocation(
                    trail.getStartLng(),
                    trail.getStartLat(),
                    RESOLVED_FROM_START_COORDINATE);
        }

        if (StringUtils.hasText(trail.getLocation())) {
            GeoLookupResponse lookupResponse = geoService.lookupLocation(trail.getLocation());
            return new ResolvedTrailLocation(
                    lookupResponse.getLng(),
                    lookupResponse.getLat(),
                    RESOLVED_FROM_LOCATION_TEXT);
        }

        throw BusinessException.badRequest("TRAIL_WEATHER_LOCATION_UNAVAILABLE", "路线缺少可用的位置数据，暂时无法查询天气");
    }

    private record ResolvedTrailLocation(BigDecimal lng, BigDecimal lat, String resolvedFrom) {
    }
}
