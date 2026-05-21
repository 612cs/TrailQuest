package com.sheng.hikingbackend.service.external.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailImportModerationResult {

    private TrailImportModerationDecision decision;
    private String reviewStatusToPersist;
    private String aiReviewStatusToPersist;
    private String riskLevel;
    private String reason;
    private String traceId;
    private LocalDateTime reviewedAt;
    private List<String> categories;
    private boolean conversationVisible;
    private boolean publiclyVisible;
}
