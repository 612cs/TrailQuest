package com.sheng.hikingbackend.vo.landscape;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandscapePredictionSourceVo {

    private String provider;
    private Boolean weatherReady;
    private Boolean astroReady;
    private Boolean lightPollutionReady;
}
