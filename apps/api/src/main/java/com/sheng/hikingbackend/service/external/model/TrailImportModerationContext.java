package com.sheng.hikingbackend.service.external.model;

import com.sheng.hikingbackend.entity.Trail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailImportModerationContext {

    private Trail trail;
    private ExternalTrailCandidate candidate;
}
