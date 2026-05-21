package com.sheng.hikingbackend.service.impl.external;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.enums.AiReviewStatus;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.enums.TrailSourceType;
import com.sheng.hikingbackend.common.enums.TrailStatus;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.external.ExternalTrailImportService;
import com.sheng.hikingbackend.service.external.ExternalTrailSearchService;
import com.sheng.hikingbackend.service.external.TrailImportDeduplicationService;
import com.sheng.hikingbackend.service.external.TrailImportModerationTrigger;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailImportItem;
import com.sheng.hikingbackend.service.external.model.ExternalTrailImportResult;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;
import com.sheng.hikingbackend.service.external.model.TrailDeduplicationMatch;
import com.sheng.hikingbackend.service.external.model.TrailImportLogAction;
import com.sheng.hikingbackend.service.external.model.TrailImportLogEntry;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationContext;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationResult;
import com.sheng.hikingbackend.vo.geo.GeoLookupResponse;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTrailImportServiceImpl implements ExternalTrailImportService {

    private static final String DEFAULT_IP = "0.0.0.0";
    private static final String DEFAULT_EXTERNAL_IMAGE =
            "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='800' height='600'%3E%3Crect width='800' height='600' fill='%23f3f4f6'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-family='sans-serif' font-size='28'%3ETrailQuest%3C/text%3E%3C/svg%3E";

    private final ExternalTrailSearchService externalTrailSearchService;
    private final TrailImportDeduplicationService trailImportDeduplicationService;
    private final TrailImportModerationTrigger trailImportModerationTrigger;
    private final ExternalTrailCandidateValidator externalTrailCandidateValidator;
    private final ExternalTrailImportProperties properties;
    private final TrailMapper trailMapper;
    private final GeoService geoService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExternalTrailImportResult searchAndImport(ExternalTrailSearchRequest request) {
        List<ExternalTrailImportItem> items = new ArrayList<>();
        List<TrailImportLogEntry> logs = new ArrayList<>();
        List<ExternalTrailCandidate> candidates = externalTrailSearchService.search(request);

        for (ExternalTrailCandidate rawCandidate : candidates) {
            ExternalTrailCandidate candidate = enrichCandidate(rawCandidate);
            if (!matchesRequestedLocation(candidate, request)) {
                logs.add(buildLog(candidate, TrailImportLogAction.SKIPPED_INCOMPLETE, null, "候选地点与查询地点不一致，已跳过", null, false, false));
                continue;
            }
            if (!hasFallbackValue(candidate)) {
                logs.add(buildLog(candidate, TrailImportLogAction.SKIPPED_INCOMPLETE, null, "候选缺少最小可导入字段", null, false, false));
                continue;
            }
            if (!externalTrailCandidateValidator.hasMinimumFields(candidate)) {
                logs.add(buildLog(candidate, TrailImportLogAction.SKIPPED_INCOMPLETE, null, "候选缺少最小可导入字段", null, false, false));
                continue;
            }

            TrailDeduplicationMatch deduplicationMatch = trailImportDeduplicationService.match(candidate);
            if (deduplicationMatch.isDuplicate()) {
                boolean publiclyVisible = TrailReviewStatus.APPROVED.getCode().equals(deduplicationMatch.getReviewStatus());
                boolean conversationVisible = publiclyVisible
                        || AiReviewStatus.APPROVED.getCode().equals(deduplicationMatch.getAiReviewStatus());
                items.add(ExternalTrailImportItem.builder()
                        .candidate(candidate)
                        .trailId(deduplicationMatch.getTrailId())
                        .created(false)
                        .conversationVisible(conversationVisible)
                        .publiclyVisible(publiclyVisible)
                        .deduplicationMatch(deduplicationMatch)
                        .moderationResult(null)
                        .build());
                logs.add(buildLog(
                        candidate,
                        TrailImportLogAction.REUSED_EXISTING,
                        deduplicationMatch.getTrailId(),
                        "命中去重规则：" + deduplicationMatch.getMatchedBy(),
                        deduplicationMatch.getAiReviewStatus(),
                        conversationVisible,
                        false));
                continue;
            }

            Long authorId = resolveAuthorId(request);
            if (authorId == null) {
                logs.add(buildLog(candidate, TrailImportLogAction.SKIPPED_MISSING_AUTHOR, null, "缺少导入归属用户，已跳过入库", null, false, false));
                continue;
            }

            Trail trail = buildImportedTrail(candidate, authorId);
            trailMapper.insert(trail);
            TrailImportModerationResult moderationResult = trailImportModerationTrigger.moderate(TrailImportModerationContext.builder()
                    .trail(trail)
                    .candidate(candidate)
                    .build());
            applyModerationResult(trail, moderationResult);
            trailMapper.updateById(trail);

            boolean conversationVisible = moderationResult.isConversationVisible();
            items.add(ExternalTrailImportItem.builder()
                    .candidate(candidate)
                    .trailId(trail.getId())
                    .created(true)
                    .conversationVisible(conversationVisible)
                    .publiclyVisible(moderationResult.isPubliclyVisible())
                    .deduplicationMatch(TrailDeduplicationMatch.notMatched())
                    .moderationResult(moderationResult)
                    .build());
            logs.add(buildLog(
                    candidate,
                    moderationResult.getDecision() == com.sheng.hikingbackend.service.external.model.TrailImportModerationDecision.REJECTED
                            ? TrailImportLogAction.REJECTED
                            : TrailImportLogAction.CREATED,
                    trail.getId(),
                    moderationResult.getReason(),
                    moderationResult.getDecision().getCode(),
                    conversationVisible,
                    moderationResult.isPubliclyVisible()));
        }

        return ExternalTrailImportResult.builder()
                .items(items)
                .importLogs(logs)
                .build();
    }

    private ExternalTrailCandidate enrichCandidate(ExternalTrailCandidate candidate) {
        ExternalTrailCandidate current = enrichCoordinates(candidate);
        return enrichGeo(current);
    }

    private ExternalTrailCandidate enrichCoordinates(ExternalTrailCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        if (candidate.getStartLng() != null && candidate.getStartLat() != null) {
            return candidate;
        }
        String locationText = firstNonBlank(
                candidate.getLocation(),
                joinLocationParts(candidate.getGeoProvince(), candidate.getGeoCity(), candidate.getGeoDistrict()),
                candidate.getName());
        if (!StringUtils.hasText(locationText)) {
            return candidate;
        }
        try {
            GeoLookupResponse lookup = geoService.lookupLocation(locationText);
            BigDecimal lookupLng = lookup == null ? null : lookup.getLng();
            BigDecimal lookupLat = lookup == null ? null : lookup.getLat();
            String lookupCountry = lookup == null ? null : lookup.getCountry();
            String lookupProvince = lookup == null ? null : lookup.getProvince();
            String lookupCity = lookup == null ? null : lookup.getCity();
            String lookupDistrict = lookup == null ? null : lookup.getDistrict();
            String lookupFormattedLocation = lookup == null ? null : lookup.getFormattedLocation();
            return candidate.toBuilder()
                    .startLng(candidate.getStartLng() == null ? lookupLng : candidate.getStartLng())
                    .startLat(candidate.getStartLat() == null ? lookupLat : candidate.getStartLat())
                    .geoCountry(StringUtils.hasText(candidate.getGeoCountry()) ? candidate.getGeoCountry() : lookupCountry)
                    .geoProvince(StringUtils.hasText(candidate.getGeoProvince()) ? candidate.getGeoProvince() : lookupProvince)
                    .geoCity(StringUtils.hasText(candidate.getGeoCity()) ? candidate.getGeoCity() : lookupCity)
                    .geoDistrict(StringUtils.hasText(candidate.getGeoDistrict()) ? candidate.getGeoDistrict() : lookupDistrict)
                    .location(StringUtils.hasText(candidate.getLocation()) ? candidate.getLocation() : lookupFormattedLocation)
                    .geoSource(StringUtils.hasText(candidate.getGeoSource()) ? candidate.getGeoSource() : "location_lookup")
                    .build();
        } catch (RuntimeException ex) {
            log.warn("External trail coordinate enrichment failed, fallback to raw candidate. name={}", candidate.getName(), ex);
            return candidate;
        }
    }

    private ExternalTrailCandidate enrichGeo(ExternalTrailCandidate candidate) {
        if (candidate == null) {
            return null;
        }
        if (candidate.getStartLng() == null || candidate.getStartLat() == null) {
            return candidate;
        }
        if (StringUtils.hasText(candidate.getGeoCity()) && StringUtils.hasText(candidate.getGeoProvince())) {
            return candidate;
        }
        try {
            ReverseGeoResponse reverseGeoResponse = geoService.reverse(candidate.getStartLng(), candidate.getStartLat());
            if (reverseGeoResponse == null) {
                return candidate;
            }
            return candidate.toBuilder()
                    .geoCountry(StringUtils.hasText(candidate.getGeoCountry()) ? candidate.getGeoCountry() : reverseGeoResponse.getCountry())
                    .geoProvince(StringUtils.hasText(candidate.getGeoProvince()) ? candidate.getGeoProvince() : reverseGeoResponse.getProvince())
                    .geoCity(StringUtils.hasText(candidate.getGeoCity()) ? candidate.getGeoCity() : reverseGeoResponse.getCity())
                    .geoDistrict(StringUtils.hasText(candidate.getGeoDistrict()) ? candidate.getGeoDistrict() : reverseGeoResponse.getDistrict())
                    .geoSource(StringUtils.hasText(candidate.getGeoSource()) ? candidate.getGeoSource() : "reverse_geo")
                    .build();
        } catch (RuntimeException ex) {
            log.warn("External trail reverse geo enrichment failed, fallback to raw candidate. name={}", candidate.getName(), ex);
            return candidate;
        }
    }

    private Long resolveAuthorId(ExternalTrailSearchRequest request) {
        if (request != null && request.getRequesterUserId() != null) {
            return request.getRequesterUserId();
        }
        return properties.getFallbackAuthorId();
    }

    private Trail buildImportedTrail(ExternalTrailCandidate candidate, Long authorId) {
        Trail trail = new Trail();
        trail.setImage(candidate.getImageUrl());
        if (!StringUtils.hasText(trail.getImage())) {
            trail.setImage(DEFAULT_EXTERNAL_IMAGE);
        }
        trail.setName(candidate.getName());
        trail.setLocation(candidate.getLocation());
        trail.setIp(DEFAULT_IP);
        trail.setStartLng(candidate.getStartLng());
        trail.setStartLat(candidate.getStartLat());
        trail.setGeoCountry(candidate.getGeoCountry());
        trail.setGeoProvince(candidate.getGeoProvince());
        trail.setGeoCity(candidate.getGeoCity());
        trail.setGeoDistrict(candidate.getGeoDistrict());
        trail.setGeoSource(candidate.getGeoSource());
        trail.setDifficulty(defaultIfBlank(candidate.getDifficulty(), "moderate"));
        trail.setDifficultyLabel(defaultIfBlank(candidate.getDifficultyLabel(), "适中"));
        trail.setPackType(defaultIfBlank(candidate.getPackType(), "light"));
        trail.setDurationType(defaultIfBlank(candidate.getDurationType(), "single_day"));
        trail.setRating(BigDecimal.ZERO.setScale(1));
        trail.setReviewCount(0);
        trail.setDistance(defaultIfBlank(candidate.getDistance(), "待补充"));
        trail.setElevation(defaultIfBlank(candidate.getElevation(), "待补充"));
        trail.setDuration(defaultIfBlank(candidate.getDuration(), "待补充"));
        trail.setDescription(candidate.getDescription());
        trail.setFavorites(0);
        trail.setLikes(0);
        trail.setAuthorId(authorId);
        trail.setSourceType(TrailSourceType.AI_WEB_IMPORT.getCode());
        trail.setSourceSite(candidate.getSourceSite());
        trail.setSourceUrl(candidate.getSourceUrl());
        trail.setSourceConfidence(candidate.getSourceConfidence());
        trail.setImportBatchNo(buildImportBatchNo());
        trail.setStatus(TrailStatus.ACTIVE.getCode());
        trail.setReviewStatus(TrailReviewStatus.PENDING.getCode());
        trail.setAiReviewStatus(AiReviewStatus.PENDING.getCode());
        trail.setAiReviewReason("等待 AI 导入审核");
        trail.setAiReviewRiskLevel("unknown");
        trail.setAiReviewCategoriesJson("[]");
        trail.setAiReviewModel("rule_based_placeholder");
        trail.setAiReviewTraceId(null);
        trail.setAiReviewedAt(null);
        trail.setReviewRemark("网络收录路线，公开展示仍需人工复核");
        trail.setReviewedBy(null);
        trail.setReviewedAt(null);
        return trail;
    }

    private void applyModerationResult(Trail trail, TrailImportModerationResult moderationResult) {
        trail.setReviewStatus(moderationResult.getReviewStatusToPersist());
        trail.setAiReviewStatus(moderationResult.getAiReviewStatusToPersist());
        trail.setAiReviewReason(moderationResult.getReason());
        trail.setAiReviewRiskLevel(moderationResult.getRiskLevel());
        trail.setAiReviewCategoriesJson(toJsonArray(moderationResult.getCategories()));
        trail.setAiReviewModel("rule_based_placeholder");
        trail.setAiReviewTraceId(moderationResult.getTraceId());
        trail.setAiReviewedAt(moderationResult.getReviewedAt());
        if (TrailReviewStatus.REJECTED.getCode().equals(moderationResult.getReviewStatusToPersist())) {
            trail.setReviewRemark(moderationResult.getReason());
            trail.setReviewedAt(moderationResult.getReviewedAt());
        } else {
            trail.setReviewRemark("网络收录路线仅允许当前对话返回，公开展示仍需人工复核");
            trail.setReviewedAt(null);
        }
    }

    private TrailImportLogEntry buildLog(
            ExternalTrailCandidate candidate,
            TrailImportLogAction action,
            Long trailId,
            String message,
            String moderationDecision,
            boolean conversationVisible,
            boolean publiclyVisible) {
        return TrailImportLogEntry.builder()
                .externalId(candidate.getExternalId())
                .trailName(candidate.getName())
                .sourceSite(candidate.getSourceSite())
                .sourceUrl(candidate.getSourceUrl())
                .action(action)
                .trailId(trailId)
                .message(message)
                .moderationDecision(moderationDecision)
                .conversationVisible(conversationVisible)
                .publiclyVisible(publiclyVisible)
                .build();
    }

    private String toJsonArray(List<String> categories) {
        try {
            return objectMapper.writeValueAsString(categories == null ? List.of() : categories);
        } catch (JsonProcessingException ex) {
            log.warn("Serialize moderation categories failed", ex);
            return "[]";
        }
    }

    private String buildImportBatchNo() {
        return "import-" + System.currentTimeMillis();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String joinLocationParts(String province, String city, String district) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(province)) {
            parts.add(province.trim());
        }
        if (StringUtils.hasText(city) && !parts.contains(city.trim())) {
            parts.add(city.trim());
        }
        if (StringUtils.hasText(district) && !parts.contains(district.trim())) {
            parts.add(district.trim());
        }
        return parts.isEmpty() ? null : String.join(" ", parts);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean matchesRequestedLocation(ExternalTrailCandidate candidate, ExternalTrailSearchRequest request) {
        if (candidate == null || request == null) {
            return true;
        }
        String requestedLocation = firstNonBlank(
                request.getGeoDistrict(),
                request.getGeoCity(),
                request.getGeoProvince(),
                request.getLocation(),
                request.getRawQuery());
        if (!StringUtils.hasText(requestedLocation)) {
            return true;
        }
        String normalizedRequested = requestedLocation.replaceAll("\\s+", "");
        String normalizedCandidate = firstNonBlank(
                candidate.getLocation(),
                candidate.getGeoDistrict(),
                candidate.getGeoCity(),
                candidate.getGeoProvince(),
                candidate.getName());
        if (!StringUtils.hasText(normalizedCandidate)) {
            return false;
        }
        normalizedCandidate = normalizedCandidate.replaceAll("\\s+", "");
        if (normalizedCandidate.contains(normalizedRequested) || normalizedRequested.contains(normalizedCandidate)) {
            return true;
        }
        if (StringUtils.hasText(request.getGeoProvince()) && !containsAny(normalizedCandidate, List.of(request.getGeoProvince()))) {
            return false;
        }
        if (StringUtils.hasText(request.getGeoCity()) && !containsAny(normalizedCandidate, List.of(request.getGeoCity()))) {
            return false;
        }
        if (StringUtils.hasText(request.getGeoDistrict()) && !containsAny(normalizedCandidate, List.of(request.getGeoDistrict()))) {
            return false;
        }
        return false;
    }

    private boolean containsAny(String source, List<String> tokens) {
        if (!StringUtils.hasText(source) || tokens == null) {
            return false;
        }
        for (String token : tokens) {
            if (StringUtils.hasText(token) && source.contains(token.replaceAll("\\s+", ""))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFallbackValue(ExternalTrailCandidate candidate) {
        if (candidate == null) {
            return false;
        }
        return StringUtils.hasText(candidate.getName())
                && StringUtils.hasText(candidate.getLocation())
                && StringUtils.hasText(candidate.getDescription())
                && StringUtils.hasText(candidate.getSourceUrl());
    }
}
