package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.vo.weather.TrailWeatherResponseVo;

public interface TrailWeatherService {

    TrailWeatherResponseVo getTrailWeather(Long trailId);
}
