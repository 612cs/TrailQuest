package com.sheng.hikingbackend.service.landscape.predictor;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sheng.hikingbackend.service.landscape.model.BigDecimalHolder;
import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.service.landscape.model.LightPollutionContext;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;

class LandscapePredictorsTest {

    @Test
    void sunriseSunsetPredictorShouldReturnStructuredResult() {
        SunriseSunsetPredictor predictor = new SunriseSunsetPredictor();

        var result = predictor.predict(buildContext());

        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getScore()).isBetween(0.0, 0.99);
        assertThat(result.getSunriseScore()).isBetween(0.0, 0.99);
        assertThat(result.getSunsetScore()).isBetween(0.0, 0.99);
        assertThat(result.getBestWindow()).isNotBlank();
        assertThat(result.getResolvedFrom()).isEqualTo("start_coordinate");
    }

    @Test
    void milkyWayPredictorShouldProduceNightWindow() {
        MilkyWayPredictor predictor = new MilkyWayPredictor();

        var result = predictor.predict(buildContext());

        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getScore()).isBetween(0.0, 0.99);
        assertThat(result.getBestWindow()).contains(":00-");
        assertThat(result.getConfidence()).isBetween(0.0, 0.99);
        assertThat(result.getVisible()).isNotNull();
    }

    @Test
    void experimentalRandomForestPredictorsShouldMarkExperimental() {
        LandscapeContext context = buildContext();

        var cloudSea = new CloudSeaRandomForestPredictor().predict(context);
        var rime = new RimeRandomForestPredictor().predict(context);
        var icicle = new IcicleRandomForestPredictor().predict(context);

        assertExperimentalCard(cloudSea);
        assertExperimentalCard(rime);
        assertExperimentalCard(icicle);
    }

    private void assertExperimentalCard(com.sheng.hikingbackend.vo.landscape.LandscapePredictionCardVo card) {
        assertThat(card.getEnabled()).isTrue();
        assertThat(card.getExperimental()).isTrue();
        assertThat(card.getScore()).isBetween(0.0, 0.99);
        assertThat(card.getConfidence()).isBetween(0.0, 0.99);
        assertThat(card.getBestWindow()).isNotBlank();
    }

    private LandscapeContext buildContext() {
        LocalDate today = LocalDate.of(2026, 3, 29);
        OffsetDateTime sunrise = OffsetDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 6, 8, 0, 0, ZoneOffset.ofHours(8));
        OffsetDateTime sunset = OffsetDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 18, 24, 0, 0, ZoneOffset.ofHours(8));

        return LandscapeContext.builder()
                .trailId(1L)
                .trailName("武功山云海线")
                .location("江西 萍乡 芦溪")
                .lng(new BigDecimal("114.12"))
                .lat(new BigDecimal("27.45"))
                .resolvedFrom("start_coordinate")
                .elevationGainMeters(new BigDecimal("920"))
                .elevationPeakMeters(new BigDecimal("1820"))
                .elevationMinMeters(new BigDecimal("340"))
                .currentWeather(CurrentWeatherSnapshot.builder()
                        .obsTime(sunrise.minusHours(2))
                        .text("多云")
                        .temp(new BigDecimal("6"))
                        .humidity(91)
                        .windSpeed(new BigDecimal("4.2"))
                        .windScale("3-4")
                        .pressure(new BigDecimal("905"))
                        .cloud(42)
                        .dew(new BigDecimal("4.8"))
                        .vis(new BigDecimal("18"))
                        .build())
                .hourlyForecast(buildHourlyForecast(today))
                .sunForecast(List.of(SunAstronomyPoint.builder()
                        .date(today)
                        .sunrise(sunrise)
                        .sunset(sunset)
                        .sunriseSolarElevation(BigDecimalHolder.builder().value(new BigDecimal("5.2")).build())
                        .sunsetSolarElevation(BigDecimalHolder.builder().value(new BigDecimal("4.9")).build())
                        .build()))
                .moonForecast(List.of(MoonAstronomyPoint.builder()
                        .date(today)
                        .moonrise(sunset.plusHours(1))
                        .moonset(sunrise.plusDays(1).minusHours(1))
                        .illumination(18)
                        .phaseName("娥眉月")
                        .build()))
                .lightPollution(LightPollutionContext.builder()
                        .ready(false)
                        .level(1.2)
                        .source("fallback-heuristic")
                        .build())
                .days(7)
                .build();
    }

    private List<HourlyWeatherPoint> buildHourlyForecast(LocalDate date) {
        OffsetDateTime start = OffsetDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.ofHours(8));
        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> {
                    boolean dawnWindow = hour >= 4 && hour <= 8;
                    boolean nightWindow = hour >= 20 || hour <= 3;
                    int cloud = dawnWindow ? 36 : nightWindow ? 24 : 58;
                    int humidity = dawnWindow ? 93 : nightWindow ? 86 : 72;
                    double temp = dawnWindow ? 1.5 + hour * 0.4 : nightWindow ? 3.0 : 8.5;
                    double dew = dawnWindow ? temp - 1.2 : nightWindow ? temp - 1.8 : temp - 4.0;
                    double wind = dawnWindow ? 4.2 : nightWindow ? 3.1 : 5.8;
                    double precip = dawnWindow ? 0.1 : nightWindow ? 0.0 : 0.3;
                    int pop = dawnWindow ? 18 : nightWindow ? 10 : 28;
                    return HourlyWeatherPoint.builder()
                            .fxTime(start.plusHours(hour))
                            .text(nightWindow ? "晴" : "多云")
                            .temp(BigDecimal.valueOf(temp))
                            .humidity(humidity)
                            .windSpeed(BigDecimal.valueOf(wind))
                            .windScale("3-4")
                            .pop(pop)
                            .precip(BigDecimal.valueOf(precip))
                            .pressure(new BigDecimal("905"))
                            .cloud(cloud)
                            .dew(BigDecimal.valueOf(dew))
                            .vis(new BigDecimal("18"))
                            .build();
                })
                .toList();
    }
}
