package com.sheng.hikingbackend.vo.weather;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherCurrentVo {

    private OffsetDateTime obsTime;
    private String text;
    private BigDecimal temp;
    private Integer humidity;
    private BigDecimal windSpeed;
    private String windScale;
    private String windDir;
    private Integer wind360;
    private BigDecimal pressure;
    private Integer cloud;
    private BigDecimal dew;
    private BigDecimal vis;
}
