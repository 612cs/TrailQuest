package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

class FirecrawlExternalTrailSearchServiceRequestTest {

    @Test
    void shouldBuildNepalSearchRequestWithMarkdownObjectFormat() throws Exception {
        FirecrawlExternalTrailSearchServiceImpl service = new FirecrawlExternalTrailSearchServiceImpl(new ExternalTrailImportProperties());
        Method method = FirecrawlExternalTrailSearchServiceImpl.class.getDeclaredMethod(
                "buildSearchRequest",
                String.class,
                ExternalTrailSearchRequest.class,
                ExternalTrailImportProperties.ProviderProperties.class);
        method.setAccessible(true);

        ExternalTrailImportProperties.ProviderProperties provider = new ExternalTrailImportProperties.ProviderProperties();
        provider.setWhitelistSites(List.of("wikiloc.com"));
        Map<String, Object> request = (Map<String, Object>) method.invoke(
                service,
                "尼泊尔 徒步路线",
                ExternalTrailSearchRequest.builder()
                        .rawQuery("帮我找找尼泊尔的徒步路线")
                        .location("尼泊尔")
                        .build(),
                provider);

        assertEquals("尼泊尔 徒步路线", request.get("query"));
        assertEquals("NP", request.get("country"));
        assertEquals("Nepal", request.get("location"));
        assertTrue(request.containsKey("scrapeOptions"));
        Map<String, Object> scrapeOptions = (Map<String, Object>) request.get("scrapeOptions");
        assertEquals(List.of(Map.of("type", "markdown")), scrapeOptions.get("formats"));
    }
}
