package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.landscape.LandscapeAstronomyService;
import com.sheng.hikingbackend.service.landscape.LandscapeMeteorologyService;
import com.sheng.hikingbackend.service.TrailWeatherService;
import com.sheng.hikingbackend.service.WeatherService;
import com.sheng.hikingbackend.service.landscape.LightPollutionService;
import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;
import com.sheng.hikingbackend.vo.weather.TrailWeatherAstroVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherCurrentVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherForecastDayVo;
import com.sheng.hikingbackend.vo.weather.TrailWeatherHourlyVo;
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
    private final LandscapeMeteorologyService landscapeMeteorologyService;
    private final LandscapeAstronomyService landscapeAstronomyService;
    private final LightPollutionService lightPollutionService;

    public TrailWeatherServiceImpl(
            TrailMapper trailMapper,
            GeoService geoService,
            WeatherService weatherService,
            LandscapeMeteorologyService landscapeMeteorologyService,
            LandscapeAstronomyService landscapeAstronomyService,
            LightPollutionService lightPollutionService) {
        this.trailMapper = trailMapper;
        this.geoService = geoService;
        this.weatherService = weatherService;
        this.landscapeMeteorologyService = landscapeMeteorologyService;
        this.landscapeAstronomyService = landscapeAstronomyService;
        this.lightPollutionService = lightPollutionService;
    }

    @Override
    public TrailWeatherResponseVo getTrailWeather(Long trailId) {
        Trail trail = trailMapper.selectActiveById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在或已删除");
        }

        ResolvedTrailLocation resolvedLocation = resolveLocation(trail);
        List<TrailWeatherForecastDayVo> forecast = Collections.emptyList();
        CurrentWeatherSnapshot currentWeather = null;
        List<HourlyWeatherPoint> hourlyForecast = Collections.emptyList();
        SunAstronomyPoint sunInfo = null;
        MoonAstronomyPoint moonInfo = null;

        boolean dailyReady = false;
        boolean currentReady = false;
        boolean hourlyReady = false;
        boolean astroReady = false;

        try {
            forecast = weatherService.getSevenDayForecast(resolvedLocation.lng(), resolvedLocation.lat());
            dailyReady = !forecast.isEmpty();
        } catch (BusinessException ex) {
            dailyReady = false;
        }

        try {
            currentWeather = landscapeMeteorologyService.getCurrentWeather(resolvedLocation.lng(), resolvedLocation.lat());
            currentReady = currentWeather != null;
        } catch (BusinessException ex) {
            currentReady = false;
        }

        try {
            hourlyForecast = landscapeMeteorologyService.getHourlyForecast(resolvedLocation.lng(), resolvedLocation.lat(), 24);
            hourlyReady = !hourlyForecast.isEmpty();
        } catch (BusinessException ex) {
            hourlyReady = false;
        }

        try {
            sunInfo = landscapeAstronomyService.getSunInfo(resolvedLocation.lng(), resolvedLocation.lat(), java.time.LocalDate.now(), null);
            moonInfo = landscapeAstronomyService.getMoonInfo(resolvedLocation.lng(), resolvedLocation.lat(), java.time.LocalDate.now());
            astroReady = sunInfo != null && moonInfo != null;
        } catch (BusinessException ex) {
            astroReady = false;
        }

        return TrailWeatherResponseVo.builder()
                .locationContext(TrailWeatherLocationContextVo.builder()
                        .lng(resolvedLocation.lng())
                        .lat(resolvedLocation.lat())
                        .resolvedFrom(resolvedLocation.resolvedFrom())
                        .build())
                .forecast(forecast)
                .current(toCurrentVo(currentWeather))
                .hourly(toHourlyVo(hourlyForecast))
                .astro(toAstroVo(sunInfo, moonInfo))
                .source(TrailWeatherSourceVo.builder()
                        .provider("qweather")
                        .cached(Boolean.FALSE)
                        .dailyReady(dailyReady)
                        .currentReady(currentReady)
                        .hourlyReady(hourlyReady)
                        .astroReady(astroReady)
                        .lightPollutionReady(lightPollutionService.resolve(
                                resolvedLocation.lng(),
                                resolvedLocation.lat(),
                                trail.getLocation()).ready())
                        .build())
                .build();
    }

    private TrailWeatherCurrentVo toCurrentVo(CurrentWeatherSnapshot currentWeather) {
        if (currentWeather == null) {
            return null;
        }
        return TrailWeatherCurrentVo.builder()
                .obsTime(currentWeather.obsTime())
                .text(currentWeather.text())
                .temp(currentWeather.temp())
                .humidity(currentWeather.humidity())
                .windSpeed(currentWeather.windSpeed())
                .windScale(currentWeather.windScale())
                .windDir(currentWeather.windDir())
                .wind360(currentWeather.wind360())
                .pressure(currentWeather.pressure())
                .cloud(currentWeather.cloud())
                .dew(currentWeather.dew())
                .vis(currentWeather.vis())
                .build();
    }

    private List<TrailWeatherHourlyVo> toHourlyVo(List<HourlyWeatherPoint> hourlyForecast) {
        if (hourlyForecast == null || hourlyForecast.isEmpty()) {
            return Collections.emptyList();
        }
        return hourlyForecast.stream()
                .map(point -> TrailWeatherHourlyVo.builder()
                        .fxTime(point.fxTime())
                        .text(point.text())
                        .temp(point.temp())
                        .humidity(point.humidity())
                        .windSpeed(point.windSpeed())
                        .windScale(point.windScale())
                        .windDir(point.windDir())
                        .wind360(point.wind360())
                        .pop(point.pop())
                        .precip(point.precip())
                        .pressure(point.pressure())
                        .cloud(point.cloud())
                        .dew(point.dew())
                        .vis(point.vis())
                        .build())
                .toList();
    }

    private TrailWeatherAstroVo toAstroVo(SunAstronomyPoint sunInfo, MoonAstronomyPoint moonInfo) {
        if (sunInfo == null && moonInfo == null) {
            return null;
        }
        return TrailWeatherAstroVo.builder()
                .sunrise(sunInfo == null ? null : sunInfo.sunrise())
                .sunset(sunInfo == null ? null : sunInfo.sunset())
                .sunriseSolarElevation(sunInfo == null || sunInfo.sunriseSolarElevation() == null || sunInfo.sunriseSolarElevation().value() == null
                        ? null
                        : sunInfo.sunriseSolarElevation().value().doubleValue())
                .sunsetSolarElevation(sunInfo == null || sunInfo.sunsetSolarElevation() == null || sunInfo.sunsetSolarElevation().value() == null
                        ? null
                        : sunInfo.sunsetSolarElevation().value().doubleValue())
                .moonrise(moonInfo == null ? null : moonInfo.moonrise())
                .moonset(moonInfo == null ? null : moonInfo.moonset())
                .moonPhase(moonInfo == null ? null : moonInfo.phaseName())
                .illumination(moonInfo == null ? null : moonInfo.illumination())
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
