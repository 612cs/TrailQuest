package com.sheng.hikingbackend.service.impl.external;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirecrawlExternalTrailSearchServiceImpl implements ExternalTrailProvider {

    private static final Pattern EBC_PATTERN = Pattern.compile("(?i)(everest base camp|\\bebc\\b|珠峰大本营)");
    private static final Pattern ANNAPURNA_PATTERN = Pattern.compile("(?i)(annapurna|act|abc|安娜普尔纳)");
    private static final Pattern NEPAL_PATTERN = Pattern.compile("(?i)(尼泊尔|nepal)");

    private final ExternalTrailImportProperties properties;

    @Override
    public ExternalTrailProviderType getProviderType() {
        return ExternalTrailProviderType.FIRECRAWL;
    }

    @Override
    public boolean supports(ExternalTrailSearchRequest request) {
        if (!ExternalTrailProvider.super.supports(request)) {
            return false;
        }
        ExternalTrailImportProperties.ProviderProperties provider = properties
                .getProviderOrDefault(ExternalImportProviderNames.FIRECRAWL);
        return properties.isEnabled() && provider.isEnabled();
    }

    @Override
    public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
        ExternalTrailImportProperties.ProviderProperties provider = properties
                .getProviderOrDefault(ExternalImportProviderNames.FIRECRAWL);
        validateProviderEnabled(provider);

        String query = resolveQuery(request);
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        try {
            FirecrawlSearchResponse response = buildRestClient(provider)
                    .post()
                    .uri("/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + provider.getApiKey())
                    .body(buildSearchRequest(query, request, provider))
                    .retrieve()
                    .body(FirecrawlSearchResponse.class);

            if (response == null || !response.success() || response.data() == null || response.data().web() == null) {
                throw invalidPayload("Firecrawl 返回了空响应或非成功状态");
            }

            return response.data().web().stream()
                    .filter(item -> item != null && StringUtils.hasText(item.url()))
                    .map(this::toCandidate)
                    .limit(resolveLimit(request.getLimit()))
                    .toList();
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.warn("Firecrawl provider response error. status={} body={}", ex.getStatusCode().value(), responseBody);
            throw BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_REQUEST_FAILED,
                    "Firecrawl provider 请求失败：" + ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw translateTimeoutOrRequestFailure(ex);
        } catch (RestClientException ex) {
            throw BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_REQUEST_FAILED,
                    "Firecrawl provider 请求失败，请稍后重试");
        }
    }

    private RestClient buildRestClient(ExternalTrailImportProperties.ProviderProperties provider) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(provider.getTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(provider.getTimeoutMs()));
        return RestClient.builder()
                .baseUrl(provider.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    private Map<String, Object> buildSearchRequest(
            String query,
            ExternalTrailSearchRequest request,
            ExternalTrailImportProperties.ProviderProperties provider) {
        return Map.of(
                "query", query,
                "limit", (int) resolveLimit(request.getLimit()),
                "country", resolveCountry(request),
                "location", resolveLocation(request),
                "includeDomains", resolveWhitelistSites(provider),
                "scrapeOptions", Map.of(
                        "formats", List.of(Map.of("type", "markdown")),
                        "onlyMainContent", true));
    }

    private String resolveCountry(ExternalTrailSearchRequest request) {
        String combined = java.util.stream.Stream.of(
                request == null ? null : request.getRawQuery(),
                request == null ? null : request.getLocation(),
                request == null ? null : request.getGeoProvince(),
                request == null ? null : request.getGeoCity(),
                request == null ? null : request.getGeoDistrict())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase(Locale.ROOT);
        if (combined.contains("尼泊尔") || combined.contains("nepal") || combined.contains("ebc") || combined.contains("everest base camp")) {
            return "NP";
        }
        return "CN";
    }

    private String resolveLocation(ExternalTrailSearchRequest request) {
        String combined = java.util.stream.Stream.of(
                request == null ? null : request.getRawQuery(),
                request == null ? null : request.getLocation(),
                request == null ? null : request.getGeoProvince(),
                request == null ? null : request.getGeoCity(),
                request == null ? null : request.getGeoDistrict())
                .filter(StringUtils::hasText)
                .map(String::trim)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase(Locale.ROOT);
        if (combined.contains("尼泊尔") || combined.contains("nepal") || combined.contains("ebc") || combined.contains("everest base camp")) {
            return "Nepal";
        }
        if (StringUtils.hasText(request == null ? null : request.getGeoProvince())
                || StringUtils.hasText(request == null ? null : request.getGeoCity())
                || StringUtils.hasText(request == null ? null : request.getGeoDistrict())) {
            return firstNonBlank(request.getGeoDistrict(), request.getGeoCity(), request.getGeoProvince(), request.getLocation(), "China");
        }
        return "China";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private List<String> resolveWhitelistSites(ExternalTrailImportProperties.ProviderProperties provider) {
        return provider.getWhitelistSites() == null ? List.of() : provider.getWhitelistSites().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private ExternalTrailCandidate toCandidate(FirecrawlSearchResponse.FirecrawlSearchItem item) {
        String location = extractLocation(item);
        return ExternalTrailCandidate.builder()
                .externalId(item.url())
                .sourceSite(extractHost(item.url()))
                .sourceUrl(item.url())
                .imageUrl(resolveImageUrl(item))
                .name(fallbackTitle(item.title(), item.url()))
                .location(location)
                .description(summarize(item.description(), item.markdown()))
                .geoSource("firecrawl_search")
                .sourceConfidence(new BigDecimal("0.72"))
                .tags(List.of("external_import", "firecrawl"))
                .build();
    }

    private String resolveImageUrl(FirecrawlSearchResponse.FirecrawlSearchItem item) {
        if (item == null) {
            return null;
        }
        if (StringUtils.hasText(item.imageUrl())) {
            return item.imageUrl().trim();
        }
        if (item.metadata() == null) {
            return null;
        }
        if (StringUtils.hasText(item.metadata().imageUrl())) {
            return item.metadata().imageUrl().trim();
        }
        if (StringUtils.hasText(item.metadata().ogImage())) {
            return item.metadata().ogImage().trim();
        }
        return null;
    }

    private String fallbackTitle(String title, String url) {
        if (StringUtils.hasText(title)) {
            return title;
        }
        return StringUtils.hasText(url) ? url : "外部路线候选";
    }

    private String summarize(String description, String markdown) {
        if (StringUtils.hasText(description)) {
            return description;
        }
        if (!StringUtils.hasText(markdown)) {
            return "Firecrawl 返回了候选页面，但尚未完成详情抽取。";
        }
        String normalized = markdown.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160);
    }

    private String extractLocation(FirecrawlSearchResponse.FirecrawlSearchItem item) {
        if (item == null) {
            return null;
        }
        String[] sources = new String[] {
                item.description(),
                item.markdown(),
                item.title(),
                item.url()
        };
        for (String source : sources) {
            String extracted = extractChineseLocation(source);
            if (StringUtils.hasText(extracted)) {
                return extracted;
            }
        }
        return null;
    }

    private String extractChineseLocation(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        for (String marker : List.of(
                "中国", "云南", "四川", "浙江", "江西", "西藏", "大理", "武功山", "莫干山", "珠峰", "珠峰大本营", "尼泊尔", "尼泊尔EBC", "安娜普尔纳")) {
            if (normalized.contains(marker)) {
                return marker;
            }
        }
        if (EBC_PATTERN.matcher(normalized).find()) {
            return "尼泊尔EBC";
        }
        if (ANNAPURNA_PATTERN.matcher(normalized).find()) {
            return "安娜普尔纳";
        }
        if (NEPAL_PATTERN.matcher(normalized).find()) {
            return "尼泊尔";
        }
        return null;
    }

    private String resolveQuery(ExternalTrailSearchRequest request) {
        if (request == null) {
            return "";
        }
        if (StringUtils.hasText(request.getRawQuery())) {
            return request.getRawQuery().trim();
        }
        if (StringUtils.hasText(request.getLocation())) {
            return request.getLocation().trim();
        }

        return List.of(request.getGeoProvince(), request.getGeoCity(), request.getGeoDistrict()).stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private long resolveLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return properties.getMaxCandidates();
        }
        return Math.min(requestedLimit, properties.getMaxCandidates());
    }

    private void validateProviderEnabled(ExternalTrailImportProperties.ProviderProperties provider) {
        if (!properties.isEnabled() || !provider.isEnabled()) {
            throw BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_DISABLED,
                    "外部导入 provider 未启用");
        }
        if (!StringUtils.hasText(provider.getApiKey()) || !StringUtils.hasText(provider.getBaseUrl())) {
            throw BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_DISABLED,
                    "Firecrawl provider 配置缺失，请检查 API Key 和 Base URL");
        }
    }

    private BusinessException translateTimeoutOrRequestFailure(ResourceAccessException ex) {
        Throwable rootCause = ex.getMostSpecificCause();
        if (rootCause instanceof SocketTimeoutException) {
            return BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_TIMEOUT,
                    "Firecrawl provider 请求超时，请稍后重试");
        }
        return BusinessException.badRequest(
                ExternalImportProviderErrorCodes.PROVIDER_REQUEST_FAILED,
                "Firecrawl provider 请求失败，请稍后重试");
    }

    private BusinessException invalidPayload(String message) {
        return BusinessException.badRequest(
                ExternalImportProviderErrorCodes.PROVIDER_PAYLOAD_INVALID,
                message);
    }

    private String extractHost(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String normalized = url.trim().toLowerCase(Locale.ROOT).replaceFirst("^https?://", "");
        int slashIndex = normalized.indexOf('/');
        return slashIndex >= 0 ? normalized.substring(0, slashIndex) : normalized;
    }
}
