package com.sheng.hikingbackend.service.landscape.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record CurrentWeatherSnapshot(
        OffsetDateTime obsTime,
        String text,
        BigDecimal temp,
        Integer humidity,
        BigDecimal windSpeed,
        String windScale,
        BigDecimal pressure,
        Integer cloud,
        BigDecimal dew,
        BigDecimal vis) {
}
