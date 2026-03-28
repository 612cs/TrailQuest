package com.sheng.hikingbackend.vo.landscape;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandscapeSunriseSunsetPredictionVo {

    private Boolean enabled;
    private Double score;
    private String level;
    private String bestWindow;
    private String summary;
    private List<String> risks;
    private Double confidence;
    private String resolvedFrom;
    private String sunriseTime;
    private String sunsetTime;
    private Double sunriseScore;
    private Double sunsetScore;
}
