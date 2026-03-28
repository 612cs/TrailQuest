package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;

import com.sheng.hikingbackend.service.landscape.model.LightPollutionContext;

public interface LightPollutionService {

    LightPollutionContext resolve(BigDecimal lng, BigDecimal lat, String locationText);
}
