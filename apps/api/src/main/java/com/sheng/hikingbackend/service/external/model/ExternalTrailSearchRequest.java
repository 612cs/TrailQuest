package com.sheng.hikingbackend.service.external.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalTrailSearchRequest {

    private ExternalTrailProviderType preferredProvider;
    private String rawQuery;
    private String location;
    private String geoProvince;
    private String geoCity;
    private String geoDistrict;
    private Integer limit;
    private Long requesterUserId;
}
