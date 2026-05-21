package com.sheng.hikingbackend.service.impl.external;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "trail.external-import")
public class ExternalTrailImportProperties {

    private boolean enabled = true;
    private int maxCandidates = 3;
    private Long fallbackAuthorId = 1L;
    private String defaultProvider = ExternalImportProviderNames.STUB;
    private Map<String, ProviderProperties> providers = buildDefaultProviders();

    public ProviderProperties getDefaultProviderProperties() {
        return getProviderOrDefault(defaultProvider);
    }

    public ProviderProperties getProviderOrDefault(String providerName) {
        if (!StringUtils.hasText(providerName)) {
            return new ProviderProperties();
        }
        ProviderProperties properties = providers.get(providerName.toLowerCase(Locale.ROOT));
        return properties == null ? new ProviderProperties() : properties;
    }

    private Map<String, ProviderProperties> buildDefaultProviders() {
        Map<String, ProviderProperties> defaults = new LinkedHashMap<>();
        defaults.put(ExternalImportProviderNames.STUB, ProviderProperties.stubDefaults());
        defaults.put(ExternalImportProviderNames.FIRECRAWL, ProviderProperties.firecrawlDefaults());
        return defaults;
    }

    @Getter
    @Setter
    public static class ProviderProperties {

        private boolean enabled = false;
        private String apiKey;
        private String baseUrl;
        private int timeoutMs = 10000;
        private List<String> whitelistSites = new ArrayList<>();

        public static ProviderProperties stubDefaults() {
            ProviderProperties properties = new ProviderProperties();
            properties.setEnabled(true);
            properties.setWhitelistSites(new ArrayList<>(List.of(
                    "demo.partner.trailquest.cn",
                    "whitelist.trailquest.cn")));
            return properties;
        }

        public static ProviderProperties firecrawlDefaults() {
            ProviderProperties properties = new ProviderProperties();
            properties.setEnabled(false);
            properties.setBaseUrl("https://api.firecrawl.dev/v2");
            properties.setTimeoutMs(10000);
            properties.setWhitelistSites(new ArrayList<>());
            return properties;
        }
    }
}
