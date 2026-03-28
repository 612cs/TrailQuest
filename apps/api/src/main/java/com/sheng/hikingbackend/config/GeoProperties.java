package com.sheng.hikingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "geo.qweather")
public class GeoProperties {

    private String baseUrl = "https://geoapi.qweather.com";
    private String projectId;
    private String keyId;
    private String privateKey;
    private long jwtTtlSeconds = 900;
}
