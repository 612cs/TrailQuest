package com.sheng.hikingbackend.common.enums;

import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum HikingPackPreference implements IEnum<String> {
    LIGHT("light"),
    HEAVY("heavy"),
    BOTH("both");

    private final String code;

    HikingPackPreference(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getValue() {
        return code;
    }

    public static HikingPackPreference fromCode(String code) {
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
