package com.sheng.hikingbackend.common.enums;

public enum TrailStatus {
    ACTIVE("active"),
    DELETED("deleted");

    private final String code;

    TrailStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
