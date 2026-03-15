package com.sheng.hikingbackend.common.enums;

import java.util.Arrays;

public enum MediaBizType {
    AVATAR("avatar"),
    TRAIL_COVER("trail_cover"),
    TRAIL_GALLERY("trail_gallery"),
    REVIEW("review");

    private final String code;

    MediaBizType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MediaBizType fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
