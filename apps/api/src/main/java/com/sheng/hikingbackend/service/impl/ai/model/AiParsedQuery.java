package com.sheng.hikingbackend.service.impl.ai.model;

import java.util.List;

import lombok.Builder;

@Builder
public record AiParsedQuery(
        AiIntent intent,
        String location,
        String geoProvince,
        String geoCity,
        String geoDistrict,
        String difficulty,
        String packType,
        String durationType,
        String distance,
        List<String> tags,
        List<String> keywords) {
}
