package com.sheng.hikingbackend.service.impl.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.external.ExternalTrailImportService;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailImportItem;
import com.sheng.hikingbackend.service.external.model.ExternalTrailImportResult;
import com.sheng.hikingbackend.service.external.model.TrailDeduplicationMatch;
import com.sheng.hikingbackend.service.impl.ai.model.AiIntent;
import com.sheng.hikingbackend.service.impl.ai.model.AiRecommendationResult;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationDecision;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationResult;
import com.sheng.hikingbackend.mapper.UserHikingProfileMapper;

@ExtendWith(MockitoExtension.class)
class AiRouteRecommendationServiceImplTest {

    @Mock
    private DashScopeChatService dashScopeChatService;
    @Mock
    private TrailService trailService;
    @Mock
    private ExternalTrailImportService externalTrailImportService;
    @Mock
    private UserHikingProfileMapper userHikingProfileMapper;

    @Test
    void planRecommendationShouldFallbackToExternalImportCardsWhenInternalTrailsAreEmpty() {
        AiRouteRecommendationServiceImpl service = new AiRouteRecommendationServiceImpl(
                dashScopeChatService,
                trailService,
                externalTrailImportService,
                userHikingProfileMapper,
                new ObjectMapper());

        when(userHikingProfileMapper.selectByUserId(7L)).thenReturn(null);
        when(dashScopeChatService.completeJson(any())).thenReturn("{\"intent\":\"trail_recommendation\",\"location\":\"杭州\",\"difficulty\":\"easy\",\"packType\":\"light\",\"durationType\":\"single_day\",\"distance\":\"medium\",\"tags\":[],\"keywords\":[\"杭州\"]}");
        when(trailService.pageTrails(any(), eq(7L))).thenReturn(PageResponse.of(List.of(), 1, 12, 0));

        ExternalTrailCandidate candidate = ExternalTrailCandidate.builder()
                .externalId("whitelist-hangzhou-longjing-jiuxi-loop")
                .sourceSite("whitelist.trailquest.cn")
                .sourceUrl("https://whitelist.trailquest.cn/routes/hangzhou-longjing-jiuxi-loop")
                .imageUrl("https://whitelist.trailquest.cn/images/hangzhou-longjing-jiuxi-loop.jpg")
                .name("杭州龙井到九溪周末轻徒步环线")
                .location("浙江 杭州 西湖")
                .description("从龙井出发串联茶园、山脊与九溪步道的单日轻装路线。")
                .difficulty("easy")
                .difficultyLabel("简单")
                .packType("light")
                .durationType("single_day")
                .distance("10 km")
                .elevation("+420 m")
                .duration("4.5h")
                .sourceConfidence(new BigDecimal("0.91"))
                .build();

        ExternalTrailImportItem importItem = ExternalTrailImportItem.builder()
                .candidate(candidate)
                .trailId(9001L)
                .created(true)
                .conversationVisible(true)
                .publiclyVisible(false)
                .deduplicationMatch(TrailDeduplicationMatch.notMatched())
                .moderationResult(TrailImportModerationResult.builder()
                        .decision(TrailImportModerationDecision.APPROVED)
                        .reviewStatusToPersist("pending")
                        .aiReviewStatusToPersist("approved")
                        .riskLevel("low")
                        .reason("白名单来源，允许在当前对话展示")
                        .traceId("trace-hz-1")
                        .conversationVisible(true)
                        .publiclyVisible(false)
                        .build())
                .build();

        when(externalTrailImportService.searchAndImport(any())).thenReturn(ExternalTrailImportResult.builder()
                .items(List.of(importItem))
                .importLogs(List.of())
                .build());

        AiRecommendationResult result = service.planRecommendation(7L, "推荐杭州周边适合周末单日徒步的路线");

        assertEquals(AiIntent.TRAIL_RECOMMENDATION, result.intent());
        assertEquals(1, result.trailCards().size());
        assertEquals("杭州龙井到九溪周末轻徒步环线", result.trailCards().get(0).getName());
        assertEquals("网络收录", result.trailCards().get(0).getSourceLabel());
        assertFalse(result.trailCards().get(0).getDetailAvailable());
    }

    @Test
    void planRecommendationShouldDetectNepalLocationForExternalFallback() {
        AiRouteRecommendationServiceImpl service = new AiRouteRecommendationServiceImpl(
                dashScopeChatService,
                trailService,
                externalTrailImportService,
                userHikingProfileMapper,
                new ObjectMapper());

        when(userHikingProfileMapper.selectByUserId(8L)).thenReturn(null);
        when(dashScopeChatService.completeJson(any())).thenReturn("{\"intent\":\"trail_recommendation\",\"location\":\"尼泊尔\",\"difficulty\":null,\"packType\":null,\"durationType\":null,\"distance\":null,\"tags\":[],\"keywords\":[\"尼泊尔\"]}");
        when(trailService.pageTrails(any(), eq(8L))).thenReturn(PageResponse.of(List.of(), 1, 12, 0));

        ExternalTrailCandidate candidate = ExternalTrailCandidate.builder()
                .externalId("whitelist-nepal-ebc-classic")
                .sourceSite("demo.partner.trailquest.cn")
                .sourceUrl("https://demo.partner.trailquest.cn/routes/nepal-ebc-classic")
                .imageUrl("https://demo.partner.trailquest.cn/images/nepal-ebc-classic.jpg")
                .name("尼泊尔 EBC 经典徒步线")
                .location("尼泊尔 EBC")
                .description("覆盖卢卡拉、南池和珠峰大本营的经典多日徒步路线。")
                .difficulty("hard")
                .difficultyLabel("困难")
                .packType("heavy")
                .durationType("multi_day")
                .distance("65 km")
                .elevation("+3200 m")
                .duration("12d")
                .sourceConfidence(new BigDecimal("0.89"))
                .build();

        ExternalTrailImportItem importItem = ExternalTrailImportItem.builder()
                .candidate(candidate)
                .trailId(9002L)
                .created(true)
                .conversationVisible(true)
                .publiclyVisible(false)
                .deduplicationMatch(TrailDeduplicationMatch.notMatched())
                .moderationResult(TrailImportModerationResult.builder()
                        .decision(TrailImportModerationDecision.APPROVED)
                        .reviewStatusToPersist("pending")
                        .aiReviewStatusToPersist("approved")
                        .riskLevel("low")
                        .reason("白名单来源，允许在当前对话展示")
                        .traceId("trace-np-1")
                        .conversationVisible(true)
                        .publiclyVisible(false)
                        .build())
                .build();

        when(externalTrailImportService.searchAndImport(any())).thenReturn(ExternalTrailImportResult.builder()
                .items(List.of(importItem))
                .importLogs(List.of())
                .build());

        AiRecommendationResult result = service.planRecommendation(8L, "帮我找找尼泊尔的徒步路线");

        assertEquals(AiIntent.TRAIL_RECOMMENDATION, result.intent());
        assertEquals(1, result.trailCards().size());
        assertEquals("尼泊尔 EBC 经典徒步线", result.trailCards().get(0).getName());
        assertEquals("网络收录", result.trailCards().get(0).getSourceLabel());
    }
}
