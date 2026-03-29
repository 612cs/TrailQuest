package com.sheng.hikingbackend.vo.weather;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailWeatherSourceVo {

    private String provider;
    private Boolean cached;
    private Boolean dailyReady;
    private Boolean currentReady;
    private Boolean hourlyReady;
    private Boolean astroReady;
    private Boolean lightPollutionReady;
}
