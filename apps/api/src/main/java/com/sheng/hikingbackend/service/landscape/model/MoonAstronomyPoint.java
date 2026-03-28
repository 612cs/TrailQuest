package com.sheng.hikingbackend.service.landscape.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import lombok.Builder;

@Builder
public record MoonAstronomyPoint(
        LocalDate date,
        OffsetDateTime moonrise,
        OffsetDateTime moonset,
        Integer illumination,
        String phaseName) {
}
