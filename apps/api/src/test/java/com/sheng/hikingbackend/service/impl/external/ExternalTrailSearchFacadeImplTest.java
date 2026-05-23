package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

class ExternalTrailSearchFacadeImplTest {

    @Test
    void searchShouldAggregateAcrossProvidersAndRespectLimit() {
        RecordingProvider stubProvider = new RecordingProvider(
                ExternalTrailProviderType.STUB_WHITELIST,
                List.of(candidate("stub-1"), candidate("stub-2")));
        RecordingProvider autoProvider = new RecordingProvider(
                ExternalTrailProviderType.AUTO,
                List.of(candidate("future-1"), candidate("future-2")));
        ExternalTrailSearchFacadeImpl facade = new ExternalTrailSearchFacadeImpl(
                new ExternalTrailProviderRegistry(buildProperties(), List.of(autoProvider, stubProvider)));

        List<ExternalTrailCandidate> result = facade.search(ExternalTrailSearchRequest.builder()
                .rawQuery("山脊")
                .limit(3)
                .build());

        assertEquals(1, autoProvider.invocationCount);
        assertEquals(1, stubProvider.invocationCount);
        assertEquals(List.of("stub-1", "stub-2", "future-1"), extractExternalIds(result));
    }

    @Test
    void searchShouldSkipEmptyOrNullProviderResponses() {
        RecordingProvider emptyProvider = new RecordingProvider(ExternalTrailProviderType.STUB_WHITELIST, List.of());
        RecordingProvider nullProvider = new RecordingProvider(ExternalTrailProviderType.AUTO, null);
        RecordingProvider autoProvider = new RecordingProvider(ExternalTrailProviderType.AUTO, List.of(candidate("fallback-1")));
        ExternalTrailSearchFacadeImpl facade = new ExternalTrailSearchFacadeImpl(
                new ExternalTrailProviderRegistry(buildProperties(), List.of(autoProvider, nullProvider, emptyProvider)));

        List<ExternalTrailCandidate> result = facade.search(ExternalTrailSearchRequest.builder()
                .rawQuery("洱海")
                .limit(2)
                .build());

        assertEquals(List.of("fallback-1"), extractExternalIds(result));
        assertEquals(1, emptyProvider.invocationCount);
        assertEquals(1, nullProvider.invocationCount);
        assertEquals(1, autoProvider.invocationCount);
    }

    @Test
    void searchShouldFallbackWhenProviderThrowsBusinessException() {
        ThrowingProvider firecrawlProvider = new ThrowingProvider(ExternalTrailProviderType.FIRECRAWL);
        RecordingProvider stubProvider = new RecordingProvider(
                ExternalTrailProviderType.STUB_WHITELIST,
                List.of(candidate("stub-fallback-1")));
        ExternalTrailSearchFacadeImpl facade = new ExternalTrailSearchFacadeImpl(
                new ExternalTrailProviderRegistry(buildProperties(), List.of(firecrawlProvider, stubProvider)));

        List<ExternalTrailCandidate> result = facade.search(ExternalTrailSearchRequest.builder()
                .rawQuery("杭州周边单日徒步")
                .limit(2)
                .build());

        assertEquals(List.of("stub-fallback-1"), extractExternalIds(result));
        assertEquals(1, firecrawlProvider.invocationCount);
        assertEquals(1, stubProvider.invocationCount);
    }

    private static ExternalTrailImportProperties buildProperties() {
        ExternalTrailImportProperties properties = new ExternalTrailImportProperties();
        properties.setDefaultProvider(ExternalImportProviderNames.FIRECRAWL);
        return properties;
    }

    private static ExternalTrailCandidate candidate(String externalId) {
        return ExternalTrailCandidate.builder()
                .externalId(externalId)
                .sourceSite("stub.local")
                .sourceUrl("https://stub.local/" + externalId)
                .imageUrl("https://stub.local/" + externalId + ".jpg")
                .name(externalId)
                .location("测试位置")
                .description("测试描述")
                .build();
    }

    private static List<String> extractExternalIds(List<ExternalTrailCandidate> candidates) {
        List<String> ids = new ArrayList<>();
        for (ExternalTrailCandidate candidate : candidates) {
            ids.add(candidate.getExternalId());
        }
        return ids;
    }

    private static class RecordingProvider implements ExternalTrailProvider {

        private final ExternalTrailProviderType providerType;
        private final List<ExternalTrailCandidate> response;
        private int invocationCount;

        private RecordingProvider(ExternalTrailProviderType providerType, List<ExternalTrailCandidate> response) {
            this.providerType = providerType;
            this.response = response;
        }

        @Override
        public ExternalTrailProviderType getProviderType() {
            return providerType;
        }

        @Override
        public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
            invocationCount++;
            return response;
        }
    }

    private static class ThrowingProvider implements ExternalTrailProvider {

        private final ExternalTrailProviderType providerType;
        private int invocationCount;

        private ThrowingProvider(ExternalTrailProviderType providerType) {
            this.providerType = providerType;
        }

        @Override
        public ExternalTrailProviderType getProviderType() {
            return providerType;
        }

        @Override
        public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
            invocationCount++;
            throw BusinessException.badRequest(
                    ExternalImportProviderErrorCodes.PROVIDER_REQUEST_FAILED,
                    "Firecrawl provider 请求失败，请稍后重试");
        }
    }
}
