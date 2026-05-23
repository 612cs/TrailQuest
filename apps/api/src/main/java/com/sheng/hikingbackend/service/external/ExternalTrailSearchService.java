package com.sheng.hikingbackend.service.external;

import java.util.List;

import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

public interface ExternalTrailSearchService {

    List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request);
}
