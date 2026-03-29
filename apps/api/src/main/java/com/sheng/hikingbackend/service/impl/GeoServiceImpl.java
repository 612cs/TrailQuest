package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.impl.support.QWeatherApiSupport;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

@Service
public class GeoServiceImpl implements GeoService {

    private final QWeatherApiSupport qWeatherApiSupport;

    public GeoServiceImpl(QWeatherApiSupport qWeatherApiSupport) {
        this.qWeatherApiSupport = qWeatherApiSupport;
    }

    @Override
    public ReverseGeoResponse reverse(BigDecimal lng, BigDecimal lat) {
        JsonNode payload = requestCityLookup(Map.of(
                "location", qWeatherApiSupport.normalizeCoordinate(lng) + "," + qWeatherApiSupport.normalizeCoordinate(lat)));

        JsonNode first = extractFirstLocation(payload, "GEO_REVERSE_EMPTY", "未查询到对应地点信息");
        String province = blankToNull(first.path("adm1").asText());
        String city = blankToNull(first.path("adm2").asText());
        String district = blankToNull(first.path("name").asText());

        return ReverseGeoResponse.builder()
                .lng(lng)
                .lat(lat)
                .country(blankToNull(first.path("country").asText()))
                .province(province)
                .city(city)
                .district(district)
                .formattedLocation(formatLocation(province, city, district))
                .build();
    }

    @Override
    public GeoLookupResponse lookupLocation(String location) {
        String normalizedLocation = blankToNull(location);
        if (!StringUtils.hasText(normalizedLocation)) {
            throw BusinessException.badRequest("GEO_LOCATION_REQUIRED", "地点文本不能为空");
        }

        JsonNode payload = requestCityLookup(Map.of("location", normalizedLocation));
        JsonNode first = extractFirstLocation(payload, "GEO_LOOKUP_EMPTY", "未查询到对应地点信息");

        BigDecimal lng = parseDecimal(first.path("lon").asText());
        BigDecimal lat = parseDecimal(first.path("lat").asText());
        String province = blankToNull(first.path("adm1").asText());
        String city = blankToNull(first.path("adm2").asText());
        String district = blankToNull(first.path("name").asText());

        if (lng == null || lat == null) {
            throw BusinessException.badRequest("GEO_LOOKUP_EMPTY", "地点坐标解析失败");
        }

        return GeoLookupResponse.builder()
                .lng(lng)
                .lat(lat)
                .country(blankToNull(first.path("country").asText()))
                .province(province)
                .city(city)
                .district(district)
                .formattedLocation(formatLocation(province, city, district))
                .build();
    }

    private JsonNode requestCityLookup(Map<String, String> queryParams) {
        var response = qWeatherApiSupport.get("/geo/v2/city/lookup", queryParams);
        JsonNode payload = response.payload();
        if (response.statusCode().isError() || payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapGeoPayloadError(payload);
        }
        return payload;
    }

    private JsonNode extractFirstLocation(JsonNode payload, String emptyCode, String emptyMessage) {
        JsonNode locationNode = payload.path("location");
        if (!locationNode.isArray() || locationNode.isEmpty()) {
            throw BusinessException.badRequest(emptyCode, emptyMessage);
        }
        return locationNode.get(0);
    }

    private BusinessException mapGeoPayloadError(JsonNode payload) {
        String code = payload == null ? null : blankToNull(payload.path("code").asText());
        if (!StringUtils.hasText(code)) {
            return BusinessException.badRequest("GEO_REVERSE_FAILED", "经纬度反查地点失败，请稍后重试");
        }
        return switch (code) {
            case "401" -> BusinessException.badRequest("GEO_QWEATHER_401", "和风鉴权失败，请检查 Project ID、Key ID 和私钥是否匹配");
            case "403" -> BusinessException.badRequest("GEO_QWEATHER_403", "当前和风凭据没有访问 GeoAPI 的权限，请检查凭据启用的 API");
            case "404" -> BusinessException.badRequest("GEO_QWEATHER_404", "和风地理接口地址不可用，请检查 GeoAPI 域名配置");
            case "429" -> BusinessException.badRequest("GEO_QWEATHER_429", "和风地理接口请求过于频繁，请稍后重试");
            default -> BusinessException.badRequest("GEO_QWEATHER_" + code, "和风地理接口返回错误，代码: " + code);
        };
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

    private String formatLocation(String province, String city, String district) {
        if (StringUtils.hasText(city) && StringUtils.hasText(district) && !city.equals(district)) {
            return city + " " + district;
        }
        if (StringUtils.hasText(city)) {
            return city;
        }
        if (StringUtils.hasText(province) && StringUtils.hasText(district) && !province.equals(district)) {
            return province + " " + district;
        }
        if (StringUtils.hasText(district)) {
            return district;
        }
        if (StringUtils.hasText(province)) {
            return province;
        }
        throw BusinessException.badRequest("GEO_LOOKUP_EMPTY", "未查询到对应地点信息");
    }
}
