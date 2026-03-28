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
    private Integer tempMax;
    private Integer tempMin;
    private String windDirDay;
    private String windScaleDay;
    private Integer humidity;
    private BigDecimal precip;
}
