package com.sheng.hikingbackend.service.impl.support;

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
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.config.GeoProperties;

import io.jsonwebtoken.Jwts;

@Component
public class QWeatherApiSupport {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final RestClient restClient;
    private final GeoProperties geoProperties;
    private volatile String cachedJwtToken;
    private volatile Instant cachedJwtExpiresAt = Instant.EPOCH;
    private volatile PrivateKey cachedPrivateKey;

    public QWeatherApiSupport(GeoProperties geoProperties) {
        this.restClient = RestClient.builder().build();
        this.geoProperties = geoProperties;
    }

    public QWeatherPayloadResponse get(String path, Map<String, String> queryParams) {
        validateJwtConfig();
        try {
            QWeatherHttpResponse response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .scheme(resolveScheme())
                                .host(resolveHost())
                                .path(path);
                        queryParams.forEach((key, value) -> {
                            if (StringUtils.hasText(value)) {
                                builder.queryParam(key, value);
                            }
                        });
                        return builder.build();
                    })
                    .header("Authorization", "Bearer " + getAuthorizationToken())
                    .exchange((request, httpResponse) -> new QWeatherHttpResponse(
                            httpResponse.getStatusCode(),
                            httpResponse.getHeaders().getOrEmpty("Content-Encoding"),
                            readAllBytes(httpResponse.getBody())));

            return new QWeatherPayloadResponse(
                    response.statusCode(),
                    parsePayload(response.body(), response.contentEncodings()));
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.badRequest("QWEATHER_REQUEST_FAILED", "和风天气服务请求失败，请稍后重试");
        }
    }

    public String normalizeCoordinate(java.math.BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private void validateJwtConfig() {
        if (!StringUtils.hasText(geoProperties.getProjectId())
                || !StringUtils.hasText(geoProperties.getKeyId())
                || !StringUtils.hasText(geoProperties.getPrivateKey())) {
            throw BusinessException.badRequest("QWEATHER_JWT_CONFIG_MISSING", "未配置和风天气 JWT 鉴权参数");
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
            Instant expiresAt = issuedAt.plusSeconds(Math.max(300, geoProperties.getJwtTtlSeconds()));
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
                throw BusinessException.badRequest("QWEATHER_PRIVATE_KEY_INVALID", "和风天气私钥格式无效");
            }
        }
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

    public record QWeatherPayloadResponse(HttpStatusCode statusCode, JsonNode payload) {
    }

    private record QWeatherHttpResponse(HttpStatusCode statusCode, List<String> contentEncodings, byte[] body) {
    }
}
