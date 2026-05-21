package com.sheng.hikingbackend.service.impl.ai.model;

import java.util.List;

import lombok.Builder;

@Builder
public record AiTrailModerationPayload(
        Long trailId,
        Long authorId,
        String sourceType,
        String name,
        String location,
        String description,
        String difficulty,
        String difficultyLabel,
        List<String> tags,
        List<String> imageUrls) {
}
