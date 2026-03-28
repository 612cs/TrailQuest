package com.sheng.hikingbackend.service;

import java.math.BigDecimal;

import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

public interface GeoService {

    ReverseGeoResponse reverse(BigDecimal lng, BigDecimal lat);
}
