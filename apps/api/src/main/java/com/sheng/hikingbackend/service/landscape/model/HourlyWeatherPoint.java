package com.sheng.hikingbackend.service.landscape.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record HourlyWeatherPoint(
        OffsetDateTime fxTime,
        String text,
        BigDecimal temp,
        Integer humidity,
        BigDecimal windSpeed,
        String windScale,
        String windDir,
        Integer wind360,
        Integer pop,
        BigDecimal precip,
        BigDecimal pressure,
        Integer cloud,
        BigDecimal dew,
        BigDecimal vis) {
}
