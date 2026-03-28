package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;
import java.util.List;

import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;

public interface LandscapeMeteorologyService {

    CurrentWeatherSnapshot getCurrentWeather(BigDecimal lng, BigDecimal lat);

    List<HourlyWeatherPoint> getHourlyForecast(BigDecimal lng, BigDecimal lat, int hours);
}
