package com.sheng.hikingbackend.common.enums;

public enum UserStatus {
    ACTIVE("active"),
    BANNED("banned"),
    DELETED("deleted");

    private final String code;

    UserStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
