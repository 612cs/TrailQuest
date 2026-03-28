package com.sheng.hikingbackend.service;

import java.math.BigDecimal;
import java.util.List;

import com.sheng.hikingbackend.vo.weather.TrailWeatherForecastDayVo;

public interface WeatherService {

    List<TrailWeatherForecastDayVo> getSevenDayForecast(BigDecimal lng, BigDecimal lat);
}
