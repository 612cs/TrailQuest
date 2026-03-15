package com.sheng.hikingbackend.common.enums;

public enum MediaFileStatus {
    UPLOADED("uploaded"),
    ACTIVE("active"),
    DELETED("deleted");

    private final String code;

    MediaFileStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
