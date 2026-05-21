package com.sheng.hikingbackend.service.impl.ai.model;

import java.util.Arrays;

public enum AiTrailModerationDecision {
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_MANUAL_REVIEW("needs_manual_review");

    private final String code;

    AiTrailModerationDecision(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AiTrailModerationDecision fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(value -> value.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElse(null);
    }
}
