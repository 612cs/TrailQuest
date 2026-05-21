package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

class ExternalTrailProviderRegistryTest {

    @Test
    void resolveProvidersShouldReturnPreferredProviderOnlyWhenSpecified() {
        TestProvider stubProvider = new TestProvider(ExternalTrailProviderType.STUB_WHITELIST);
        TestProvider autoProvider = new TestProvider(ExternalTrailProviderType.AUTO);
        ExternalTrailProviderRegistry registry = new ExternalTrailProviderRegistry(buildProperties(), List.of(autoProvider, stubProvider));

        List<ExternalTrailProvider> resolved = registry.resolveProviders(ExternalTrailSearchRequest.builder()
                .preferredProvider(ExternalTrailProviderType.STUB_WHITELIST)
                .rawQuery("武功山")
                .build());

        assertEquals(1, resolved.size());
        assertSame(stubProvider, resolved.get(0));
    }

    @Test
    void resolveProvidersShouldReturnOrderedSupportedProvidersForAutoMode() {
        TestProvider unsupportedProvider = new TestProvider(ExternalTrailProviderType.AUTO) {
            @Override
            public boolean supports(ExternalTrailSearchRequest request) {
                return false;
            }
        };
        TestProvider stubProvider = new TestProvider(ExternalTrailProviderType.STUB_WHITELIST);
        TestProvider autoProvider = new TestProvider(ExternalTrailProviderType.AUTO);
        ExternalTrailProviderRegistry registry = new ExternalTrailProviderRegistry(buildProperties(), List.of(autoProvider, unsupportedProvider, stubProvider));

        List<ExternalTrailProvider> resolved = registry.resolveProviders(ExternalTrailSearchRequest.builder()
                .preferredProvider(ExternalTrailProviderType.AUTO)
                .rawQuery("莫干山")
                .build());

        assertEquals(2, resolved.size());
        assertSame(stubProvider, resolved.get(0));
        assertSame(autoProvider, resolved.get(1));
    }

    @Test
    void getProviderShouldReturnEmptyForAutoAndUnknownProvider() {
        TestProvider stubProvider = new TestProvider(ExternalTrailProviderType.STUB_WHITELIST);
        ExternalTrailProviderRegistry registry = new ExternalTrailProviderRegistry(buildProperties(), List.of(stubProvider));

        assertTrue(registry.getProvider(null).isEmpty());
        assertTrue(registry.getProvider(ExternalTrailProviderType.AUTO).isEmpty());
        assertSame(stubProvider, registry.getProvider(ExternalTrailProviderType.STUB_WHITELIST).orElseThrow());
    }

    @Test
    void resolveProvidersShouldPreferConfiguredDefaultProviderWhenAvailable() {
        ExternalTrailImportProperties properties = buildProperties();
        properties.setDefaultProvider(ExternalImportProviderNames.FIRECRAWL);
        TestProvider stubProvider = new TestProvider(ExternalTrailProviderType.STUB_WHITELIST);
        TestProvider firecrawlProvider = new TestProvider(ExternalTrailProviderType.FIRECRAWL);
        ExternalTrailProviderRegistry registry = new ExternalTrailProviderRegistry(properties, List.of(stubProvider, firecrawlProvider));

        List<ExternalTrailProvider> resolved = registry.resolveProviders(ExternalTrailSearchRequest.builder()
                .rawQuery("EBC")
                .build());

        assertEquals(2, resolved.size());
        assertSame(firecrawlProvider, resolved.get(0));
        assertSame(stubProvider, resolved.get(1));
    }

    private ExternalTrailImportProperties buildProperties() {
        return new ExternalTrailImportProperties();
    }

    private static class TestProvider implements ExternalTrailProvider {

        private final ExternalTrailProviderType providerType;

        private TestProvider(ExternalTrailProviderType providerType) {
            this.providerType = providerType;
        }

        @Override
        public ExternalTrailProviderType getProviderType() {
            return providerType;
        }

        @Override
        public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
            return List.of();
        }
    }
}
