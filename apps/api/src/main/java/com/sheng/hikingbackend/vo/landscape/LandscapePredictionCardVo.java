package com.sheng.hikingbackend.vo.landscape;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LandscapePredictionCardVo {

    private Boolean enabled;
    private Boolean experimental;
    private Double score;
    private String level;
    private String bestWindow;
    private String summary;
    private List<String> risks;
    private Double confidence;
    private String resolvedFrom;
}
