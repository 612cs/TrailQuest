package com.sheng.hikingbackend.service.external.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailDeduplicationMatch {

    private boolean duplicate;
    private Long trailId;
    private String matchedBy;
    private String reviewStatus;
    private String aiReviewStatus;

    public static TrailDeduplicationMatch notMatched() {
        return TrailDeduplicationMatch.builder()
                .duplicate(false)
                .build();
    }
}
