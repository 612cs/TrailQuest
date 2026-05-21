package com.sheng.hikingbackend.service.impl.external;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.ExternalTrailSearchService;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTrailSearchFacadeImpl implements ExternalTrailSearchService {

    private final ExternalTrailProviderRegistry providerRegistry;

    @Override
    public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
        List<ExternalTrailProvider> providers = providerRegistry.resolveProviders(request);
        if (providers.isEmpty()) {
            return List.of();
        }

        int maxResults = resolveLimit(request);
        List<ExternalTrailCandidate> candidates = new ArrayList<>();
        for (ExternalTrailProvider provider : providers) {
            if (candidates.size() >= maxResults) {
                break;
            }
            List<ExternalTrailCandidate> providerCandidates;
            try {
                providerCandidates = provider.search(request);
            } catch (BusinessException ex) {
                log.warn(
                        "External trail provider failed and will fallback. provider={} code={} message={}",
                        provider.getProviderType(),
                        ex.getCode(),
                        ex.getMessage());
                continue;
            } catch (RuntimeException ex) {
                log.warn(
                        "External trail provider failed with unexpected error and will fallback. provider={}",
                        provider.getProviderType(),
                        ex);
                continue;
            }
            if (providerCandidates == null || providerCandidates.isEmpty()) {
                continue;
            }
            int remaining = maxResults - candidates.size();
            candidates.addAll(providerCandidates.stream()
                    .limit(remaining)
                    .toList());
        }
        return List.copyOf(candidates);
    }

    private int resolveLimit(ExternalTrailSearchRequest request) {
        if (request == null || request.getLimit() == null || request.getLimit() <= 0) {
            return Integer.MAX_VALUE;
        }
        return request.getLimit();
    }
}
