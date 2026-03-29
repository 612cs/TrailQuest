package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.impl.support.QWeatherApiSupport;

@Service
public class LandscapeMeteorologyServiceImpl implements LandscapeMeteorologyService {

    private final QWeatherApiSupport qWeatherApiSupport;

    public LandscapeMeteorologyServiceImpl(QWeatherApiSupport qWeatherApiSupport) {
        this.qWeatherApiSupport = qWeatherApiSupport;
    }

    @Override
    public CurrentWeatherSnapshot getCurrentWeather(BigDecimal lng, BigDecimal lat) {
        JsonNode payload = request("/v7/weather/now", lng, lat);
        JsonNode now = payload.path("now");
        if (now.isMissingNode() || now.isEmpty()) {
            throw BusinessException.badRequest("LANDSCAPE_WEATHER_NOW_EMPTY", "缺少实时天气数据");
        }
        return CurrentWeatherSnapshot.builder()
                .obsTime(parseDateTime(now.path("obsTime").asText()))
                .text(blankToNull(now.path("text").asText()))
                .temp(parseDecimal(now.path("temp").asText()))
                .humidity(parseInteger(now.path("humidity").asText()))
                .windSpeed(parseDecimal(now.path("windSpeed").asText()))
                .windScale(blankToNull(now.path("windScale").asText()))
                .windDir(blankToNull(now.path("windDir").asText()))
                .wind360(parseInteger(now.path("wind360").asText()))
                .pressure(parseDecimal(now.path("pressure").asText()))
                .cloud(parseInteger(now.path("cloud").asText()))
                .dew(parseDecimal(now.path("dew").asText()))
                .vis(parseDecimal(now.path("vis").asText()))
                .build();
    }

    @Override
    public List<HourlyWeatherPoint> getHourlyForecast(BigDecimal lng, BigDecimal lat, int hours) {
        String path = hours >= 168 ? "/v7/weather/168h" : hours >= 72 ? "/v7/weather/72h" : "/v7/weather/24h";
        JsonNode payload = request(path, lng, lat);
        JsonNode hourly = payload.path("hourly");
        if (!hourly.isArray() || hourly.isEmpty()) {
            throw BusinessException.badRequest("LANDSCAPE_WEATHER_HOURLY_EMPTY", "缺少逐小时天气预报数据");
        }
        return java.util.stream.StreamSupport.stream(hourly.spliterator(), false)
                .map(item -> HourlyWeatherPoint.builder()
                        .fxTime(parseDateTime(item.path("fxTime").asText()))
                        .text(blankToNull(item.path("text").asText()))
                        .temp(parseDecimal(item.path("temp").asText()))
                        .humidity(parseInteger(item.path("humidity").asText()))
                        .windSpeed(parseDecimal(item.path("windSpeed").asText()))
                        .windScale(blankToNull(item.path("windScale").asText()))
                        .windDir(blankToNull(item.path("windDir").asText()))
                        .wind360(parseInteger(item.path("wind360").asText()))
                        .pop(parseInteger(item.path("pop").asText()))
                        .precip(parseDecimal(item.path("precip").asText()))
                        .pressure(parseDecimal(item.path("pressure").asText()))
                        .cloud(parseInteger(item.path("cloud").asText()))
                        .dew(parseDecimal(item.path("dew").asText()))
                        .vis(parseDecimal(item.path("vis").asText()))
                        .build())
                .limit(hours)
                .toList();
    }

    private JsonNode request(String path, BigDecimal lng, BigDecimal lat) {
        String location = qWeatherApiSupport.normalizeCoordinate(lng) + "," + qWeatherApiSupport.normalizeCoordinate(lat);
        var response = qWeatherApiSupport.get(path, Map.of("location", location));
        JsonNode payload = response.payload();
        if (response.statusCode().isError() || payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapError(payload);
        }
        return payload;
    }

    private BusinessException mapError(JsonNode payload) {
        String code = payload == null ? null : blankToNull(payload.path("code").asText());
        if (!StringUtils.hasText(code)) {
            return BusinessException.badRequest("LANDSCAPE_WEATHER_FAILED", "景观预测天气数据查询失败");
        }
        return BusinessException.badRequest("LANDSCAPE_WEATHER_" + code, "景观预测天气接口返回错误，代码: " + code);
    }

    private OffsetDateTime parseDateTime(String value) {
        try {
            return StringUtils.hasText(value) ? OffsetDateTime.parse(value.trim()) : null;
        } catch (Exception ex) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        try {
            return StringUtils.hasText(value) ? new BigDecimal(value.trim()) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return StringUtils.hasText(value) ? Integer.valueOf(value.trim()) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
