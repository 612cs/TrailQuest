package com.sheng.hikingbackend.service.impl.support;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackParseResult {

    private String sourceFormat;
    private String geoJson;
    private Integer trackPointsCount;
    private Integer waypointCount;
    private BigDecimal startLng;
    private BigDecimal startLat;
    private BigDecimal endLng;
    private BigDecimal endLat;
    private BigDecimal bboxMinLng;
    private BigDecimal bboxMinLat;
    private BigDecimal bboxMaxLng;
    private BigDecimal bboxMaxLat;
    private BigDecimal distanceMeters;
    private BigDecimal elevationGainMeters;
    private BigDecimal elevationLossMeters;
    private Long durationSeconds;
}
