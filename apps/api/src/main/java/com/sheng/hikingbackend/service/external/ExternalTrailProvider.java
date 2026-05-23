package com.sheng.hikingbackend.service.external;

import java.util.List;

import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

public interface ExternalTrailProvider {

    ExternalTrailProviderType getProviderType();

    default boolean supports(ExternalTrailSearchRequest request) {
        return request == null
                || request.getPreferredProvider() == null
                || request.getPreferredProvider() == ExternalTrailProviderType.AUTO
                || request.getPreferredProvider() == getProviderType();
    }

    List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request);
}
