package com.sheng.hikingbackend.common.enums;

import java.util.Arrays;

public enum HikingTrailStyle {
    CITY_WEEKEND("city_weekend"),
    LONG_DISTANCE("long_distance"),
    BOTH("both");

    private final String code;

    HikingTrailStyle(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static HikingTrailStyle fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
