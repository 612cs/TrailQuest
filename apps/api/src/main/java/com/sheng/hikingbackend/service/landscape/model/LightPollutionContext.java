package com.sheng.hikingbackend.service.landscape.model;

import lombok.Builder;

@Builder
public record LightPollutionContext(
        boolean ready,
        double level,
        String source) {
}
