package com.sheng.hikingbackend.vo.landscape;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandscapePredictionResponseVo {

    private LandscapeSunriseSunsetPredictionVo sunriseSunset;
    private LandscapeMilkyWayPredictionVo milkyWay;
    private LandscapePredictionCardVo cloudSea;
    private LandscapePredictionCardVo rime;
    private LandscapePredictionCardVo icicle;
    private LandscapePredictionSourceVo source;
}
