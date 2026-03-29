package com.sheng.hikingbackend.vo.weather;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherForecastDayVo {

    private String date;
    private String fxDate;
    private String week;
    private String textDay;
    private String textNight;
    private String iconDay;
    private String iconNight;
    private Integer tempMax;
    private Integer tempMin;
    private Integer wind360Day;
    private String windDirDay;
    private String windScaleDay;
    private Integer windSpeedDay;
    private Integer wind360Night;
    private String windDirNight;
    private String windScaleNight;
    private Integer windSpeedNight;
    private Integer humidity;
    private Integer pressure;
    private Integer cloud;
    private Integer uvIndex;
    private Integer vis;
    private BigDecimal precip;
    private String sunrise;
    private String sunset;
}
