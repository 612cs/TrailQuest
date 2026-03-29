package com.sheng.hikingbackend.vo.weather;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherAstroVo {

    private OffsetDateTime sunrise;
    private OffsetDateTime sunset;
    private Double sunriseSolarElevation;
    private Double sunsetSolarElevation;
    private OffsetDateTime moonrise;
    private OffsetDateTime moonset;
    private String moonPhase;
    private Integer illumination;
}
