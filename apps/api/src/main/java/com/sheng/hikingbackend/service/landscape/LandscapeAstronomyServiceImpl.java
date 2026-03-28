package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.impl.support.QWeatherApiSupport;
import com.sheng.hikingbackend.service.landscape.model.BigDecimalHolder;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;

@Service
public class LandscapeAstronomyServiceImpl implements LandscapeAstronomyService {

    private final QWeatherApiSupport qWeatherApiSupport;

    public LandscapeAstronomyServiceImpl(QWeatherApiSupport qWeatherApiSupport) {
        this.qWeatherApiSupport = qWeatherApiSupport;
    }

    @Override
    public SunAstronomyPoint getSunInfo(BigDecimal lng, BigDecimal lat, LocalDate date, BigDecimal altitudeMeters) {
        JsonNode payload = request("/v7/astronomy/sun", lng, lat, date);
        OffsetDateTime sunrise = parseDateTime(payload.path("sunrise").asText(), date);
        OffsetDateTime sunset = parseDateTime(payload.path("sunset").asText(), date);
        return SunAstronomyPoint.builder()
                .date(date)
                .sunrise(sunrise)
                .sunset(sunset)
                .sunriseSolarElevation(sunrise == null ? null : getSolarElevation(lng, lat, date, sunrise.format(java.time.format.DateTimeFormatter.ofPattern("HHmm")), altitudeMeters))
                .sunsetSolarElevation(sunset == null ? null : getSolarElevation(lng, lat, date, sunset.format(java.time.format.DateTimeFormatter.ofPattern("HHmm")), altitudeMeters))
                .build();
    }

    @Override
    public MoonAstronomyPoint getMoonInfo(BigDecimal lng, BigDecimal lat, LocalDate date) {
        JsonNode payload = request("/v7/astronomy/moon", lng, lat, date);
        JsonNode moonPhase = payload.path("moonPhase");
        JsonNode firstPhase = moonPhase.isArray() && !moonPhase.isEmpty() ? moonPhase.get(0) : moonPhase;
        return MoonAstronomyPoint.builder()
                .date(date)
                .moonrise(parseDateTime(payload.path("moonrise").asText(), date))
                .moonset(parseDateTime(payload.path("moonset").asText(), date))
                .illumination(parseInteger(firstPhase.path("illumination").asText()))
                .phaseName(blankToNull(firstPhase.path("name").asText()))
                .build();
    }

    @Override
    public BigDecimalHolder getSolarElevation(BigDecimal lng, BigDecimal lat, LocalDate date, String time, BigDecimal altitudeMeters) {
        String location = qWeatherApiSupport.normalizeCoordinate(lng) + "," + qWeatherApiSupport.normalizeCoordinate(lat);
        var response = qWeatherApiSupport.get("/v7/astronomy/solar-elevation-angle", Map.of(
                "location", location,
                "date", date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
                "time", normalizeTime(time),
                "tz", "+0800",
                "alt", altitudeMeters == null ? "0" : altitudeMeters.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString()));
        JsonNode payload = response.payload();
        if (response.statusCode().isError() || payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapError(payload);
        }
        return BigDecimalHolder.builder()
                .value(parseDecimal(payload.path("solarElevationAngle").asText()))
                .build();
    }

    private JsonNode request(String path, BigDecimal lng, BigDecimal lat, LocalDate date) {
        String location = qWeatherApiSupport.normalizeCoordinate(lng) + "," + qWeatherApiSupport.normalizeCoordinate(lat);
        var response = qWeatherApiSupport.get(path, Map.of(
                "location", location,
                "date", date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)));
        JsonNode payload = response.payload();
        if (response.statusCode().isError() || payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapError(payload);
        }
        return payload;
    }

    private BusinessException mapError(JsonNode payload) {
        String code = payload == null ? null : blankToNull(payload.path("code").asText());
        if (!StringUtils.hasText(code)) {
            return BusinessException.badRequest("LANDSCAPE_ASTRO_FAILED", "景观预测天文数据查询失败");
        }
        return BusinessException.badRequest("LANDSCAPE_ASTRO_" + code, "景观预测天文接口返回错误，代码: " + code);
    }

    private OffsetDateTime parseDateTime(String value, LocalDate date) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (Exception ex) {
            try {
                return OffsetDateTime.of(date, java.time.LocalTime.parse(value.trim()), ZoneOffset.ofHours(8));
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String normalizeTime(String time) {
        if (!StringUtils.hasText(time)) {
            return "0600";
        }
        return time.trim().replace(":", "").replaceAll("[^0-9]", "");
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
