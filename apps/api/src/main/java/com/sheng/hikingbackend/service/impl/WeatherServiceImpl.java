package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.WeatherService;
import com.sheng.hikingbackend.service.impl.support.QWeatherApiSupport;
import com.sheng.hikingbackend.vo.weather.TrailWeatherForecastDayVo;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final QWeatherApiSupport qWeatherApiSupport;

    public WeatherServiceImpl(QWeatherApiSupport qWeatherApiSupport) {
        this.qWeatherApiSupport = qWeatherApiSupport;
    }

    @Override
    public List<TrailWeatherForecastDayVo> getSevenDayForecast(BigDecimal lng, BigDecimal lat) {
        String location = qWeatherApiSupport.normalizeCoordinate(lng) + "," + qWeatherApiSupport.normalizeCoordinate(lat);
        var response = qWeatherApiSupport.get("/v7/grid-weather/7d", Map.of("location", location));
        JsonNode payload = response.payload();

        if (response.statusCode().isError() || payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapWeatherPayloadError(payload);
        }

        JsonNode daily = payload.path("daily");
        if (!daily.isArray() || daily.isEmpty()) {
            throw BusinessException.badRequest("WEATHER_FORECAST_EMPTY", "未查询到未来七天天气数据");
        }

        return java.util.stream.StreamSupport.stream(daily.spliterator(), false)
                .map(this::toForecastDay)
                .toList();
    }

    private TrailWeatherForecastDayVo toForecastDay(JsonNode day) {
        String fxDate = day.path("fxDate").asText();
        return TrailWeatherForecastDayVo.builder()
                .date(fxDate)
                .fxDate(fxDate)
                .week(resolveWeekLabel(fxDate))
                .textDay(blankToNull(day.path("textDay").asText()))
                .textNight(blankToNull(day.path("textNight").asText()))
                .iconDay(blankToNull(day.path("iconDay").asText()))
                .iconNight(blankToNull(day.path("iconNight").asText()))
                .tempMax(parseInteger(day.path("tempMax").asText()))
                .tempMin(parseInteger(day.path("tempMin").asText()))
                .wind360Day(parseInteger(day.path("wind360Day").asText()))
                .windDirDay(blankToNull(day.path("windDirDay").asText()))
                .windScaleDay(blankToNull(day.path("windScaleDay").asText()))
                .windSpeedDay(parseInteger(day.path("windSpeedDay").asText()))
                .wind360Night(parseInteger(day.path("wind360Night").asText()))
                .windDirNight(blankToNull(day.path("windDirNight").asText()))
                .windScaleNight(blankToNull(day.path("windScaleNight").asText()))
                .windSpeedNight(parseInteger(day.path("windSpeedNight").asText()))
                .humidity(parseInteger(day.path("humidity").asText()))
                .pressure(parseInteger(day.path("pressure").asText()))
                .cloud(parseInteger(day.path("cloud").asText()))
                .uvIndex(parseInteger(day.path("uvIndex").asText()))
                .vis(parseInteger(day.path("vis").asText()))
                .precip(parseDecimal(day.path("precip").asText()))
                .sunrise(blankToNull(day.path("sunrise").asText()))
                .sunset(blankToNull(day.path("sunset").asText()))
                .build();
    }

    private BusinessException mapWeatherPayloadError(JsonNode payload) {
        String code = payload == null ? null : blankToNull(payload.path("code").asText());
        if (!StringUtils.hasText(code)) {
            return BusinessException.badRequest("WEATHER_FORECAST_FAILED", "未来七天天气查询失败，请稍后重试");
        }
        return switch (code) {
            case "401" -> BusinessException.badRequest("WEATHER_QWEATHER_401", "和风天气鉴权失败，请检查 Project ID、Key ID 和私钥是否匹配");
            case "403" -> BusinessException.badRequest("WEATHER_QWEATHER_403", "当前和风凭据没有访问天气 API 的权限，请检查启用的 API 列表");
            case "404" -> BusinessException.badRequest("WEATHER_QWEATHER_404", "和风天气接口地址不可用，请检查 API Host 配置");
            case "429" -> BusinessException.badRequest("WEATHER_QWEATHER_429", "和风天气接口请求过于频繁，请稍后重试");
            default -> BusinessException.badRequest("WEATHER_QWEATHER_" + code, "和风天气接口返回错误，代码: " + code);
        };
    }

    private String resolveWeekLabel(String fxDate) {
        try {
            DayOfWeek dayOfWeek = LocalDate.parse(fxDate).getDayOfWeek();
            return switch (dayOfWeek) {
                case MONDAY -> "周一";
                case TUESDAY -> "周二";
                case WEDNESDAY -> "周三";
                case THURSDAY -> "周四";
                case FRIDAY -> "周五";
                case SATURDAY -> "周六";
                case SUNDAY -> "周日";
            };
        } catch (Exception ex) {
            return fxDate;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return StringUtils.hasText(value) ? Integer.valueOf(value.trim()) : null;
        } catch (NumberFormatException ex) {
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

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
