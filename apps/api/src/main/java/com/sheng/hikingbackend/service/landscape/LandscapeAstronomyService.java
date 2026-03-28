package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sheng.hikingbackend.service.landscape.model.BigDecimalHolder;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;

public interface LandscapeAstronomyService {

    SunAstronomyPoint getSunInfo(BigDecimal lng, BigDecimal lat, LocalDate date, BigDecimal altitudeMeters);

    MoonAstronomyPoint getMoonInfo(BigDecimal lng, BigDecimal lat, LocalDate date);

    BigDecimalHolder getSolarElevation(BigDecimal lng, BigDecimal lat, LocalDate date, String time, BigDecimal altitudeMeters);
}
