package com.sheng.hikingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "trail.geo.backfill")
public class TrailGeoBackfillProperties {

    private boolean enabled = false;
}
