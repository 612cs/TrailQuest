package com.sheng.hikingbackend.common.enums;

import java.util.Arrays;

public enum HikingExperienceLevel {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    EXPERT("expert");

    private final String code;

    HikingExperienceLevel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static HikingExperienceLevel fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
