package com.sheng.hikingbackend.common.enums;

public enum AiReviewStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    NEEDS_MANUAL_REVIEW("needs_manual_review"),
    FAILED("failed");

    private final String code;

    AiReviewStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
