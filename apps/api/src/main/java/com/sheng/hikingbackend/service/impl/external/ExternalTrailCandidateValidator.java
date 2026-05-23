package com.sheng.hikingbackend.service.impl.external;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;

@Component
public class ExternalTrailCandidateValidator {

    public boolean hasMinimumFields(ExternalTrailCandidate candidate) {
        if (candidate == null) {
            return false;
        }
        return StringUtils.hasText(candidate.getName())
                && StringUtils.hasText(candidate.getLocation())
                && StringUtils.hasText(candidate.getDescription())
                && StringUtils.hasText(candidate.getSourceUrl());
    }
}
