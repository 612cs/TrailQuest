package com.sheng.hikingbackend.vo.trail;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailTrackVo {

    private Boolean hasTrack;
    private String sourceFormat;
    private String originalFileName;
    private String downloadUrl;
    private JsonNode geoJson;
    private TrackPointVo startPoint;
    private TrackPointVo endPoint;
    private TrackBoundsVo bounds;
    private BigDecimal distanceMeters;
    private BigDecimal elevationGainMeters;
    private BigDecimal elevationLossMeters;
    private Long durationSeconds;

    @Getter
    @Builder
    public static class TrackPointVo {
        private BigDecimal lng;
        private BigDecimal lat;
    }

    @Getter
    @Builder
    public static class TrackBoundsVo {
        private BigDecimal minLng;
        private BigDecimal minLat;
        private BigDecimal maxLng;
        private BigDecimal maxLat;
    }
}
