package com.sheng.hikingbackend.service.external.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalTrailImportItem {

    private ExternalTrailCandidate candidate;
    private Long trailId;
    private boolean created;
    private boolean conversationVisible;
    private boolean publiclyVisible;
    private TrailDeduplicationMatch deduplicationMatch;
    private TrailImportModerationResult moderationResult;
}
