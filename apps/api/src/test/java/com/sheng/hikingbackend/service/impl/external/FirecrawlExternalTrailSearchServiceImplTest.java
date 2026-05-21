package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;

class FirecrawlExternalTrailSearchServiceImplTest {

    @Test
    void shouldUseFirecrawlV2DefaultBaseUrl() {
        ExternalTrailImportProperties properties = new ExternalTrailImportProperties();

        assertEquals("https://api.firecrawl.dev/v2", properties.getProviderOrDefault(ExternalImportProviderNames.FIRECRAWL).getBaseUrl());
    }

    @Test
    void shouldExtractNepalEbcLocationFromSearchResult() throws Exception {
        FirecrawlExternalTrailSearchServiceImpl service = new FirecrawlExternalTrailSearchServiceImpl(new ExternalTrailImportProperties());
        FirecrawlSearchResponse.FirecrawlSearchItem item = new FirecrawlSearchResponse.FirecrawlSearchItem(
                "https://example.com/routes/ebc",
                "尼泊尔 EBC 环线",
                "Everest Base Camp trekking in Nepal",
                null,
                "EBC trek in Nepal",
                new FirecrawlSearchResponse.Metadata(null, null, null));

        Method method = FirecrawlExternalTrailSearchServiceImpl.class.getDeclaredMethod("toCandidate", FirecrawlSearchResponse.FirecrawlSearchItem.class);
        method.setAccessible(true);
        ExternalTrailCandidate candidate = (ExternalTrailCandidate) method.invoke(service, item);

        assertEquals("https://example.com/routes/ebc", candidate.getExternalId());
        assertEquals("example.com", candidate.getSourceSite());
        assertEquals("尼泊尔EBC", candidate.getLocation());
        assertEquals("尼泊尔 EBC 环线", candidate.getName());
        assertTrue(candidate.getDescription().contains("Everest Base Camp"));
        assertEquals(new BigDecimal("0.72"), candidate.getSourceConfidence());
        assertEquals(List.of("external_import", "firecrawl"), candidate.getTags());
    }

    @Test
    void shouldFallbackToUrlWhenTitleMissing() throws Exception {
        FirecrawlExternalTrailSearchServiceImpl service = new FirecrawlExternalTrailSearchServiceImpl(new ExternalTrailImportProperties());
        FirecrawlSearchResponse.FirecrawlSearchItem item = new FirecrawlSearchResponse.FirecrawlSearchItem(
                "https://example.com/routes/annapurna",
                null,
                null,
                null,
                null,
                new FirecrawlSearchResponse.Metadata(null, null, null));

        Method method = FirecrawlExternalTrailSearchServiceImpl.class.getDeclaredMethod("toCandidate", FirecrawlSearchResponse.FirecrawlSearchItem.class);
        method.setAccessible(true);
        ExternalTrailCandidate candidate = (ExternalTrailCandidate) method.invoke(service, item);

        assertEquals("https://example.com/routes/annapurna", candidate.getName());
        assertFalse(candidate.getDescription().isBlank());
    }

    @Test
    void shouldResolveCountryWithoutNullPointer() throws Exception {
        FirecrawlExternalTrailSearchServiceImpl service = new FirecrawlExternalTrailSearchServiceImpl(new ExternalTrailImportProperties());
        Method method = FirecrawlExternalTrailSearchServiceImpl.class.getDeclaredMethod("resolveCountry", com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest.class);
        ReflectionUtils.makeAccessible(method);
        String country = (String) method.invoke(service, new Object[] { null });
        assertEquals("CN", country);
    }
}
