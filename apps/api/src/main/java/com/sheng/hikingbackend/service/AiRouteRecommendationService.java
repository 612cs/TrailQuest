package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.service.impl.ai.model.AiRecommendationResult;

public interface AiRouteRecommendationService {

    AiRecommendationResult planRecommendation(Long userId, String message);
}
