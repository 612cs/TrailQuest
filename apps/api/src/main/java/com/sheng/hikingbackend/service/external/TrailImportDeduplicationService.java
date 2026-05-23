package com.sheng.hikingbackend.service.external;

import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.TrailDeduplicationMatch;

public interface TrailImportDeduplicationService {

    TrailDeduplicationMatch match(ExternalTrailCandidate candidate);
}
