package com.sheng.hikingbackend.common.enums;

public enum TrailSourceType {
    USER_UPLOAD("user_upload"),
    AI_WEB_IMPORT("ai_web_import"),
    INTERNAL_CURATED("internal_curated");

    private final String code;

    TrailSourceType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
