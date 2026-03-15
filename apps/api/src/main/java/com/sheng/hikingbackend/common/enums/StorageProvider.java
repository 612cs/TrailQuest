package com.sheng.hikingbackend.common.enums;

public enum StorageProvider {
    ALIYUN_OSS("aliyun_oss");

    private final String code;

    StorageProvider(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
