package com.sheng.hikingbackend.vo.geo;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeoLookupResponse {

    private BigDecimal lng;
    private BigDecimal lat;
    private String province;
    private String city;
    private String district;
    private String formattedLocation;
}
