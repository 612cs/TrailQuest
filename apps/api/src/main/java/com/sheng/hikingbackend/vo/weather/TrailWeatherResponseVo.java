package com.sheng.hikingbackend.vo.weather;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherResponseVo {

    private TrailWeatherLocationContextVo locationContext;
    private List<TrailWeatherForecastDayVo> forecast;
    private TrailWeatherCurrentVo current;
    private List<TrailWeatherHourlyVo> hourly;
    private TrailWeatherAstroVo astro;
    private TrailWeatherSourceVo source;
}
