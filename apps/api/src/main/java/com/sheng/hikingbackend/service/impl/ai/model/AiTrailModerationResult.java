package com.sheng.hikingbackend.service.impl.ai.model;

import java.util.List;

import lombok.Builder;

@Builder
public record AiTrailModerationResult(
        AiTrailModerationDecision decision,
        String riskLevel,
        List<String> riskCategories,
        String reason,
        String modelName,
        boolean fallbackByError) {
}
