package com.sheng.hikingbackend.service.impl.external;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.service.external.TrailImportModerationTrigger;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationContext;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationDecision;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationResult;

@Service
public class RuleBasedTrailImportModerationTrigger implements TrailImportModerationTrigger {

    private static final List<String> HIGH_RISK_KEYWORDS = List.of("黄赌毒", "色情", "赌博", "毒品", "招嫖", "开车群");

    @Override
    public TrailImportModerationResult moderate(TrailImportModerationContext context) {
        ExternalTrailCandidate candidate = context.getCandidate();
        LocalDateTime reviewedAt = LocalDateTime.now();
        String content = ((candidate.getName() == null ? "" : candidate.getName()) + " "
                + (candidate.getDescription() == null ? "" : candidate.getDescription())).toLowerCase(Locale.ROOT);

        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (content.contains(keyword.toLowerCase(Locale.ROOT))) {
                return TrailImportModerationResult.builder()
                        .decision(TrailImportModerationDecision.REJECTED)
                        .reviewStatusToPersist(TrailReviewStatus.REJECTED.getCode())
                        .aiReviewStatusToPersist(TrailImportModerationDecision.REJECTED.getCode())
                        .riskLevel("high")
                        .reason("命中高风险关键词，已拒绝导入")
                        .traceId(buildTraceId())
                        .reviewedAt(reviewedAt)
                        .categories(List.of("content_safety"))
                        .conversationVisible(false)
                        .publiclyVisible(false)
                        .build();
            }
        }

        if (!StringUtils.hasText(candidate.getGeoCity()) && !StringUtils.hasText(candidate.getGeoDistrict())) {
            return TrailImportModerationResult.builder()
                    .decision(TrailImportModerationDecision.NEEDS_MANUAL_REVIEW)
                    .reviewStatusToPersist(TrailReviewStatus.PENDING.getCode())
                    .aiReviewStatusToPersist(TrailImportModerationDecision.NEEDS_MANUAL_REVIEW.getCode())
                    .riskLevel("medium")
                    .reason("结构化地理信息不足，需人工复核后公开")
                    .traceId(buildTraceId())
                    .reviewedAt(reviewedAt)
                    .categories(List.of("business_risk"))
                    .conversationVisible(false)
                    .publiclyVisible(false)
                    .build();
        }

        return TrailImportModerationResult.builder()
                .decision(TrailImportModerationDecision.APPROVED)
                .reviewStatusToPersist(TrailReviewStatus.PENDING.getCode())
                .aiReviewStatusToPersist(TrailImportModerationDecision.APPROVED.getCode())
                .riskLevel("low")
                .reason("白名单来源且字段完整，可在当前对话返回，公开展示仍需人工复核")
                .traceId(buildTraceId())
                .reviewedAt(reviewedAt)
                .categories(List.of("whitelist_source"))
                .conversationVisible(true)
                .publiclyVisible(false)
                .build();
    }

    private String buildTraceId() {
        return "trail-import-" + UUID.randomUUID();
    }
}
