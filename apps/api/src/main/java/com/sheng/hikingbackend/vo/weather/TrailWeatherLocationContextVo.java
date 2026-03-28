package com.sheng.hikingbackend.vo.weather;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherLocationContextVo {

    private BigDecimal lng;
    private BigDecimal lat;
    private String resolvedFrom;
}
