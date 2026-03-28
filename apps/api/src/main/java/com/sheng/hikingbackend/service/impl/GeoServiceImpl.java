package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.config.GeoProperties;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

import io.jsonwebtoken.Jwts;

@Service
public class GeoServiceImpl implements GeoService {

    private static final Logger log = LoggerFactory.getLogger(GeoServiceImpl.class);
    private static final com.fasterxml.jackson.databind.json.JsonMapper JSON_MAPPER = com.fasterxml.jackson.databind.json.JsonMapper
            .builder()
            .build();
    private final RestClient restClient;
    private final GeoProperties geoProperties;
    private volatile String cachedJwtToken;
    private volatile Instant cachedJwtExpiresAt = Instant.EPOCH;
    private volatile PrivateKey cachedPrivateKey;

    public GeoServiceImpl(GeoProperties geoProperties) {
        this.restClient = RestClient.builder().build();
        this.geoProperties = geoProperties;
    }

    @Override
    public ReverseGeoResponse reverse(BigDecimal lng, BigDecimal lat) {
        validateJwtConfig();

        String location = normalizeCoordinate(lng) + "," + normalizeCoordinate(lat);
        String authorizationToken = getAuthorizationToken();
        JsonNode payload;
        try {
            QWeatherResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme(resolveScheme())
                            .host(resolveHost())
                            .path("/geo/v2/city/lookup")
                            .queryParam("location", location)
                            .build())
                    .header("Authorization", "Bearer " + authorizationToken)
                    .exchange((request, httpResponse) -> new QWeatherResponse(
                            httpResponse.getStatusCode(),
                            httpResponse.getHeaders().getOrEmpty("Content-Encoding"),
                            readAllBytes(httpResponse.getBody())));
            payload = parsePayload(response.body(), response.contentEncodings());
            if (response.statusCode().isError()) {
                log.warn("QWeather reverse geo request failed with status {} and body {}", response.statusCode(), payload);
                throw mapQWeatherPayloadError(payload);
            }
        } catch (RestClientResponseException ex) {
            log.warn("QWeather reverse geo request failed with status {} and body {}", ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw mapQWeatherError(ex.getResponseBodyAsString());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("QWeather reverse geo request failed before receiving a valid response", ex);
            throw BusinessException.badRequest("GEO_REVERSE_FAILED", "经纬度反查地点失败，请稍后重试");
        }

        if (payload == null || !"200".equals(payload.path("code").asText())) {
            throw mapQWeatherPayloadError(payload);
        }

        JsonNode locationNode = payload.path("location");
        if (!locationNode.isArray() || locationNode.isEmpty()) {
            throw BusinessException.badRequest("GEO_REVERSE_EMPTY", "未查询到对应地点信息");
        }

        JsonNode first = locationNode.get(0);
        String province = blankToNull(first.path("adm1").asText());
        String city = blankToNull(first.path("adm2").asText());
        String district = blankToNull(first.path("name").asText());

        return ReverseGeoResponse.builder()
                .lng(lng)
                .lat(lat)
                .province(province)
                .city(city)
                .district(district)
                .formattedLocation(formatLocation(province, city, district))
                .build();
    }

    private void validateJwtConfig() {
        if (!StringUtils.hasText(geoProperties.getProjectId())
                || !StringUtils.hasText(geoProperties.getKeyId())
                || !StringUtils.hasText(geoProperties.getPrivateKey())) {
            throw BusinessException.badRequest("GEO_JWT_CONFIG_MISSING", "未配置和风地理服务 JWT 鉴权参数");
        }
    }

    private String getAuthorizationToken() {
        Instant now = Instant.now();
        Instant refreshAt = cachedJwtExpiresAt.minusSeconds(60);
        if (StringUtils.hasText(cachedJwtToken) && now.isBefore(refreshAt)) {
            return cachedJwtToken;
        }
        synchronized (this) {
            now = Instant.now();
            refreshAt = cachedJwtExpiresAt.minusSeconds(60);
            if (StringUtils.hasText(cachedJwtToken) && now.isBefore(refreshAt)) {
                return cachedJwtToken;
            }

            Instant issuedAt = now.minusSeconds(30);
            Instant expiresAt = issuedAt.plusSeconds(resolveJwtTtlSeconds());
            cachedJwtToken = Jwts.builder()
                    .header()
                    .keyId(geoProperties.getKeyId())
                    .and()
                    .subject(geoProperties.getProjectId())
                    .issuedAt(Date.from(issuedAt))
                    .expiration(Date.from(expiresAt))
                    .signWith(getPrivateKey(), Jwts.SIG.EdDSA)
                    .compact();
            cachedJwtExpiresAt = expiresAt;
            return cachedJwtToken;
        }
    }

    private long resolveJwtTtlSeconds() {
        return Math.max(300, geoProperties.getJwtTtlSeconds());
    }

    private PrivateKey getPrivateKey() {
        if (cachedPrivateKey != null) {
            return cachedPrivateKey;
        }
        synchronized (this) {
            if (cachedPrivateKey != null) {
                return cachedPrivateKey;
            }
            try {
                String pem = geoProperties.getPrivateKey()
                        .replace("\\n", "\n")
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "");
                byte[] keyBytes = Base64.getDecoder().decode(pem);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                cachedPrivateKey = KeyFactory.getInstance("Ed25519").generatePrivate(keySpec);
                return cachedPrivateKey;
            } catch (Exception ex) {
                throw BusinessException.badRequest("GEO_JWT_PRIVATE_KEY_INVALID", "和风地理服务私钥格式无效");
            }
        }
    }

    private String normalizeCoordinate(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String resolveScheme() {
        String baseUrl = geoProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl) || !baseUrl.contains("://")) {
            return "https";
        }
        return baseUrl.substring(0, baseUrl.indexOf("://"));
    }

    private String resolveHost() {
        String baseUrl = geoProperties.getBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return "geoapi.qweather.com";
        }
        String normalized = baseUrl.replaceFirst("^https?://", "");
        int slashIndex = normalized.indexOf('/');
        return slashIndex >= 0 ? normalized.substring(0, slashIndex) : normalized;
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
        throw BusinessException.badRequest("GEO_REVERSE_EMPTY", "未查询到对应地点信息");
    }

    private BusinessException mapQWeatherError(String responseBody) {
        try {
            JsonNode payload = JSON_MAPPER.readTree(responseBody);
            return mapQWeatherPayloadError(payload);
        } catch (Exception ex) {
            return BusinessException.badRequest("GEO_REVERSE_FAILED", "经纬度反查地点失败，请稍后重试");
        }
    }

    private BusinessException mapQWeatherPayloadError(JsonNode payload) {
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

    private JsonNode parsePayload(byte[] body, List<String> contentEncodings) throws IOException {
        byte[] jsonBytes = isGzip(contentEncodings, body) ? gunzip(body) : body;
        return JSON_MAPPER.readTree(jsonBytes);
    }

    private boolean isGzip(List<String> contentEncodings, byte[] body) {
        if (contentEncodings != null) {
            for (String value : contentEncodings) {
                if (StringUtils.hasText(value) && value.toLowerCase().contains("gzip")) {
                    return true;
                }
            }
        }
        return body != null && body.length >= 2 && (body[0] == (byte) 0x1f) && (body[1] == (byte) 0x8b);
    }

    private byte[] gunzip(byte[] compressed) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressed));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            gzipInputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            inputStream.transferTo(outputStream);
            return outputStream.toByteArray();
        }
    }

    private record QWeatherResponse(HttpStatusCode statusCode, List<String> contentEncodings, byte[] body) {
    }
}
