package com.sheng.hikingbackend.service.external.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailImportLogEntry {

    private String externalId;
    private String trailName;
    private String sourceSite;
    private String sourceUrl;
    private TrailImportLogAction action;
    private Long trailId;
    private String message;
    private String moderationDecision;
    private boolean conversationVisible;
    private boolean publiclyVisible;
}
