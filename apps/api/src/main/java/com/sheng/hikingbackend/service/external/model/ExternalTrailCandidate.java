package com.sheng.hikingbackend.service.external.model;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ExternalTrailCandidate {

    private String externalId;
    private String sourceSite;
    private String sourceUrl;
    private String imageUrl;
    private String name;
    private String location;
    private String description;
    private BigDecimal startLng;
    private BigDecimal startLat;
    private String geoCountry;
    private String geoProvince;
    private String geoCity;
    private String geoDistrict;
    private String geoSource;
    private String difficulty;
    private String difficultyLabel;
    private String packType;
    private String durationType;
    private String distance;
    private String elevation;
    private String duration;
    private BigDecimal sourceConfidence;
    private List<String> tags;
}
