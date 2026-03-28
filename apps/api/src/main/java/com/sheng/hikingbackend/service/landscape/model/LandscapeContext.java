package com.sheng.hikingbackend.service.landscape.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;

@Builder
public record LandscapeContext(
        Long trailId,
        String trailName,
        String location,
        BigDecimal lng,
        BigDecimal lat,
        String resolvedFrom,
        BigDecimal elevationGainMeters,
        BigDecimal elevationPeakMeters,
        BigDecimal elevationMinMeters,
        CurrentWeatherSnapshot currentWeather,
        List<HourlyWeatherPoint> hourlyForecast,
        List<SunAstronomyPoint> sunForecast,
        List<MoonAstronomyPoint> moonForecast,
        LightPollutionContext lightPollution,
        boolean weatherReady,
        boolean astroReady,
        int days) {
}
