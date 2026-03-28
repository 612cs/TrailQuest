package com.sheng.hikingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.common.enums.TrailStatus;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.WeatherService;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;
import com.sheng.hikingbackend.vo.weather.TrailWeatherForecastDayVo;

@ExtendWith(MockitoExtension.class)
class TrailWeatherServiceImplTest {

    private static final Long TRAIL_ID = 2001L;

    @Mock
    private TrailMapper trailMapper;
    @Mock
    private GeoService geoService;
    @Mock
    private WeatherService weatherService;

    private TrailWeatherServiceImpl trailWeatherService;

    @BeforeEach
    void setUp() {
        trailWeatherService = new TrailWeatherServiceImpl(trailMapper, geoService, weatherService);
    }

    @Test
    void shouldUseStartCoordinatesFirst() {
        Trail trail = buildTrail();
        trail.setStartLng(new BigDecimal("114.12"));
        trail.setStartLat(new BigDecimal("27.45"));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(weatherService.getSevenDayForecast(trail.getStartLng(), trail.getStartLat())).thenReturn(List.of(buildForecastDay()));

        var result = trailWeatherService.getTrailWeather(TRAIL_ID);

        assertEquals("start_coordinate", result.getLocationContext().getResolvedFrom());
        assertEquals(new BigDecimal("114.12"), result.getLocationContext().getLng());
        assertEquals(new BigDecimal("27.45"), result.getLocationContext().getLat());
        assertEquals("qweather", result.getSource().getProvider());
        verify(geoService, never()).lookupLocation(trail.getLocation());
    }

    @Test
    void shouldFallbackToLocationLookup() {
        Trail trail = buildTrail();
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(geoService.lookupLocation("江西 萍乡")).thenReturn(GeoLookupResponse.builder()
                .lng(new BigDecimal("114.11"))
                .lat(new BigDecimal("27.46"))
                .province("江西省")
                .city("萍乡")
                .district("芦溪")
                .formattedLocation("萍乡 芦溪")
                .build());
        when(weatherService.getSevenDayForecast(new BigDecimal("114.11"), new BigDecimal("27.46")))
                .thenReturn(List.of(buildForecastDay()));

        var result = trailWeatherService.getTrailWeather(TRAIL_ID);

        assertEquals("location_text", result.getLocationContext().getResolvedFrom());
        assertEquals(new BigDecimal("114.11"), result.getLocationContext().getLng());
        assertEquals(new BigDecimal("27.46"), result.getLocationContext().getLat());
        verify(geoService).lookupLocation("江西 萍乡");
    }

    @Test
    void shouldRejectWhenNoUsableLocationExists() {
        Trail trail = buildTrail();
        trail.setLocation("   ");
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> trailWeatherService.getTrailWeather(TRAIL_ID));

        assertEquals("TRAIL_WEATHER_LOCATION_UNAVAILABLE", exception.getCode());
        verify(weatherService, never()).getSevenDayForecast(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectWhenTrailMissing() {
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> trailWeatherService.getTrailWeather(TRAIL_ID));

        assertEquals("TRAIL_NOT_FOUND", exception.getCode());
    }

    private Trail buildTrail() {
        Trail trail = new Trail();
        trail.setId(TRAIL_ID);
        trail.setStatus(TrailStatus.ACTIVE.getCode());
        trail.setLocation("江西 萍乡");
        return trail;
    }

    private TrailWeatherForecastDayVo buildForecastDay() {
        return TrailWeatherForecastDayVo.builder()
                .date("2026-03-29")
                .fxDate("2026-03-29")
                .week("周日")
                .textDay("多云")
                .textNight("阴")
                .tempMax(24)
                .tempMin(16)
                .windDirDay("北风")
                .windScaleDay("1-3")
                .humidity(62)
                .build();
    }
}
