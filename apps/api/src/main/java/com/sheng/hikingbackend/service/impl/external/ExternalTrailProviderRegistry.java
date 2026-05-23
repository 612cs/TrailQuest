package com.sheng.hikingbackend.service.impl.external;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

@Component
public class ExternalTrailProviderRegistry {

    private final ExternalTrailImportProperties properties;
    private final List<ExternalTrailProvider> providersInOrder;
    private final Map<ExternalTrailProviderType, ExternalTrailProvider> providersByType;

    public ExternalTrailProviderRegistry(ExternalTrailImportProperties properties, List<ExternalTrailProvider> providers) {
        this.properties = properties;
        this.providersInOrder = List.copyOf(providers);
        this.providersByType = providers.stream()
                .collect(Collectors.toUnmodifiableMap(ExternalTrailProvider::getProviderType, Function.identity(), (left, right) -> left));
    }

    public Optional<ExternalTrailProvider> getProvider(ExternalTrailProviderType providerType) {
        if (providerType == null || providerType == ExternalTrailProviderType.AUTO) {
            return Optional.empty();
        }
        return Optional.ofNullable(providersByType.get(providerType));
    }

    public List<ExternalTrailProvider> resolveProviders(ExternalTrailSearchRequest request) {
        ExternalTrailProviderType preferredProvider = request == null ? null : request.getPreferredProvider();
        if (preferredProvider != null && preferredProvider != ExternalTrailProviderType.AUTO) {
            return getProvider(preferredProvider)
                    .filter(provider -> provider.supports(request))
                    .stream()
                    .toList();
        }

        ExternalTrailProviderType defaultProvider = resolveDefaultProviderType();
        if (defaultProvider != null && defaultProvider != ExternalTrailProviderType.AUTO) {
            List<ExternalTrailProvider> resolved = providersInOrder.stream()
                    .filter(provider -> provider.supports(request))
                    .sorted(Comparator.comparingInt(provider -> priorityOf(provider.getProviderType(), defaultProvider)))
                    .toList();
            if (!resolved.isEmpty()) {
                return resolved;
            }
        }

        return providersInOrder.stream()
                .filter(provider -> provider.supports(request))
                .sorted(Comparator.comparingInt(provider -> priorityOf(provider.getProviderType(), null)))
                .toList();
    }

    private ExternalTrailProviderType resolveDefaultProviderType() {
        String configured = properties.getDefaultProvider();
        if (configured == null) {
            return null;
        }
        return switch (configured.trim().toLowerCase()) {
            case ExternalImportProviderNames.STUB -> ExternalTrailProviderType.STUB_WHITELIST;
            case ExternalImportProviderNames.FIRECRAWL -> ExternalTrailProviderType.FIRECRAWL;
            case "", "auto" -> ExternalTrailProviderType.AUTO;
            default -> null;
        };
    }

    private int priorityOf(ExternalTrailProviderType providerType, ExternalTrailProviderType defaultProvider) {
        if (defaultProvider != null && providerType == defaultProvider) {
            return Integer.MIN_VALUE;
        }
        return switch (providerType) {
            case FIRECRAWL -> 0;
            case STUB_WHITELIST -> 10;
            case AUTO -> 100;
        };
    }
}
