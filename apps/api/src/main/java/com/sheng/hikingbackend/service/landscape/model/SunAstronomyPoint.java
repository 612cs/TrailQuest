package com.sheng.hikingbackend.service.landscape.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record SunAstronomyPoint(
        LocalDate date,
        OffsetDateTime sunrise,
        OffsetDateTime sunset,
        BigDecimalHolder sunriseSolarElevation,
        BigDecimalHolder sunsetSolarElevation) {
}
