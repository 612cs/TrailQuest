package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.vo.landscape.LandscapePredictionResponseVo;

public interface LandscapePredictionService {

    LandscapePredictionResponseVo getTrailPrediction(Long trailId, int days);
}
