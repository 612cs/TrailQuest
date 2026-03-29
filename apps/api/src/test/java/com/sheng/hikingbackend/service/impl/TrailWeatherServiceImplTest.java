package com.sheng.hikingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import com.sheng.hikingbackend.service.landscape.LandscapeAstronomyService;
import com.sheng.hikingbackend.service.landscape.LandscapeMeteorologyService;
import com.sheng.hikingbackend.service.landscape.LightPollutionService;
import com.sheng.hikingbackend.service.landscape.model.BigDecimalHolder;
import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LightPollutionContext;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;
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
    @Mock
    private LandscapeMeteorologyService landscapeMeteorologyService;
    @Mock
    private LandscapeAstronomyService landscapeAstronomyService;
    @Mock
    private LightPollutionService lightPollutionService;

    private TrailWeatherServiceImpl trailWeatherService;

    @BeforeEach
    void setUp() {
        trailWeatherService = new TrailWeatherServiceImpl(
                trailMapper,
                geoService,
                weatherService,
                landscapeMeteorologyService,
                landscapeAstronomyService,
                lightPollutionService);
    }

    @Test
    void shouldUseStartCoordinatesFirst() {
        Trail trail = buildTrail();
        trail.setStartLng(new BigDecimal("114.12"));
        trail.setStartLat(new BigDecimal("27.45"));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(weatherService.getSevenDayForecast(trail.getStartLng(), trail.getStartLat())).thenReturn(List.of(buildForecastDay()));
        when(landscapeMeteorologyService.getCurrentWeather(trail.getStartLng(), trail.getStartLat())).thenReturn(buildCurrentWeather());
        when(landscapeMeteorologyService.getHourlyForecast(trail.getStartLng(), trail.getStartLat(), 24)).thenReturn(List.of(buildHourlyWeather()));
        when(landscapeAstronomyService.getSunInfo(trail.getStartLng(), trail.getStartLat(), LocalDate.now(), null)).thenReturn(buildSunInfo());
        when(landscapeAstronomyService.getMoonInfo(trail.getStartLng(), trail.getStartLat(), LocalDate.now())).thenReturn(buildMoonInfo());
        when(lightPollutionService.resolve(trail.getStartLng(), trail.getStartLat(), trail.getLocation()))
                .thenReturn(LightPollutionContext.builder().ready(false).level(2.5).source("fallback-heuristic").build());

        var result = trailWeatherService.getTrailWeather(TRAIL_ID);

        assertEquals("start_coordinate", result.getLocationContext().getResolvedFrom());
        assertEquals(new BigDecimal("114.12"), result.getLocationContext().getLng());
        assertEquals(new BigDecimal("27.45"), result.getLocationContext().getLat());
        assertEquals("qweather", result.getSource().getProvider());
        assertEquals(Boolean.TRUE, result.getSource().getDailyReady());
        assertEquals(Boolean.TRUE, result.getSource().getCurrentReady());
        assertEquals(Boolean.TRUE, result.getSource().getHourlyReady());
        assertEquals(Boolean.TRUE, result.getSource().getAstroReady());
        assertEquals("多云", result.getCurrent().getText());
        assertEquals(1, result.getHourly().size());
        assertEquals("娥眉月", result.getAstro().getMoonPhase());
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
        when(lightPollutionService.resolve(new BigDecimal("114.11"), new BigDecimal("27.46"), trail.getLocation()))
                .thenReturn(LightPollutionContext.builder().ready(false).level(2.5).source("fallback-heuristic").build());

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

    @Test
    void shouldGracefullyDegradeWhenAstronomyFails() {
        Trail trail = buildTrail();
        trail.setStartLng(new BigDecimal("114.12"));
        trail.setStartLat(new BigDecimal("27.45"));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(weatherService.getSevenDayForecast(trail.getStartLng(), trail.getStartLat())).thenReturn(List.of(buildForecastDay()));
        when(landscapeMeteorologyService.getCurrentWeather(trail.getStartLng(), trail.getStartLat())).thenReturn(buildCurrentWeather());
        when(landscapeMeteorologyService.getHourlyForecast(trail.getStartLng(), trail.getStartLat(), 24)).thenReturn(List.of(buildHourlyWeather()));
        when(landscapeAstronomyService.getSunInfo(trail.getStartLng(), trail.getStartLat(), LocalDate.now(), null))
                .thenThrow(BusinessException.badRequest("LANDSCAPE_ASTRO_FAILED", "天文失败"));
        when(lightPollutionService.resolve(trail.getStartLng(), trail.getStartLat(), trail.getLocation()))
                .thenReturn(LightPollutionContext.builder().ready(false).level(2.5).source("fallback-heuristic").build());

        var result = trailWeatherService.getTrailWeather(TRAIL_ID);

        assertNotNull(result.getForecast());
        assertNotNull(result.getCurrent());
        assertNotNull(result.getHourly());
        assertEquals(Boolean.FALSE, result.getSource().getAstroReady());
        assertEquals(null, result.getAstro());
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

    private CurrentWeatherSnapshot buildCurrentWeather() {
        return CurrentWeatherSnapshot.builder()
                .obsTime(OffsetDateTime.of(2026, 3, 29, 8, 0, 0, 0, ZoneOffset.ofHours(8)))
                .text("多云")
                .temp(new BigDecimal("16"))
                .humidity(80)
                .windSpeed(new BigDecimal("3.2"))
                .windScale("1-3")
                .windDir("东北风")
                .wind360(45)
                .pressure(new BigDecimal("1008"))
                .cloud(60)
                .dew(new BigDecimal("12"))
                .vis(new BigDecimal("18"))
                .build();
    }

    private HourlyWeatherPoint buildHourlyWeather() {
        return HourlyWeatherPoint.builder()
                .fxTime(OffsetDateTime.of(2026, 3, 29, 9, 0, 0, 0, ZoneOffset.ofHours(8)))
                .text("小雨")
                .temp(new BigDecimal("17"))
                .humidity(84)
                .windSpeed(new BigDecimal("4.1"))
                .windScale("1-3")
                .windDir("东北风")
                .wind360(50)
                .pop(72)
                .precip(new BigDecimal("0.8"))
                .pressure(new BigDecimal("1007"))
                .cloud(68)
                .dew(new BigDecimal("13"))
                .vis(new BigDecimal("16"))
                .build();
    }

    private SunAstronomyPoint buildSunInfo() {
        return SunAstronomyPoint.builder()
                .date(LocalDate.now())
                .sunrise(OffsetDateTime.of(2026, 3, 29, 6, 8, 0, 0, ZoneOffset.ofHours(8)))
                .sunset(OffsetDateTime.of(2026, 3, 29, 18, 22, 0, 0, ZoneOffset.ofHours(8)))
                .sunriseSolarElevation(BigDecimalHolder.builder().value(new BigDecimal("5.2")).build())
                .sunsetSolarElevation(BigDecimalHolder.builder().value(new BigDecimal("4.9")).build())
                .build();
    }

    private MoonAstronomyPoint buildMoonInfo() {
        return MoonAstronomyPoint.builder()
                .date(LocalDate.now())
                .moonrise(OffsetDateTime.of(2026, 3, 29, 20, 0, 0, 0, ZoneOffset.ofHours(8)))
                .moonset(OffsetDateTime.of(2026, 3, 30, 6, 0, 0, 0, ZoneOffset.ofHours(8)))
                .illumination(24)
                .phaseName("娥眉月")
                .build();
    }
}
