package com.sheng.hikingbackend.service.external.model;

public enum TrailImportModerationDecision {
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_MANUAL_REVIEW("needs_manual_review");

    private final String code;

    TrailImportModerationDecision(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
