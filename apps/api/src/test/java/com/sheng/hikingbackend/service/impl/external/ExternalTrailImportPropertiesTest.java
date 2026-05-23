package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ExternalTrailImportPropertiesTest {

    @Test
    void shouldKeepSafeDefaultsForOfflineStubProvider() {
        ExternalTrailImportProperties properties = new ExternalTrailImportProperties();
        ExternalTrailImportProperties.ProviderProperties stub = properties.getProviderOrDefault(ExternalImportProviderNames.STUB);
        ExternalTrailImportProperties.ProviderProperties firecrawl = properties.getProviderOrDefault(ExternalImportProviderNames.FIRECRAWL);

        assertTrue(properties.isEnabled());
        assertEquals(3, properties.getMaxCandidates());
        assertEquals(1L, properties.getFallbackAuthorId());
        assertEquals(ExternalImportProviderNames.STUB, properties.getDefaultProvider());
        assertTrue(stub.isEnabled());
        assertTrue(stub.getWhitelistSites().contains("demo.partner.trailquest.cn"));
        assertTrue(stub.getWhitelistSites().contains("whitelist.trailquest.cn"));
        assertFalse(firecrawl.isEnabled());
        assertEquals("https://api.firecrawl.dev/v2", firecrawl.getBaseUrl());
    }

    @Test
    void shouldReturnEmptyProviderConfigForUnknownOrBlankName() {
        ExternalTrailImportProperties properties = new ExternalTrailImportProperties();

        ExternalTrailImportProperties.ProviderProperties unknown = properties.getProviderOrDefault("unknown");
        ExternalTrailImportProperties.ProviderProperties blank = properties.getProviderOrDefault(" ");

        assertNotNull(unknown);
        assertNotNull(blank);
        assertFalse(unknown.isEnabled());
        assertFalse(blank.isEnabled());
    }
}
