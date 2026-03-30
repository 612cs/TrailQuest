package com.sheng.hikingbackend.common.enums;

public enum TrailReviewStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String code;

    TrailReviewStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
