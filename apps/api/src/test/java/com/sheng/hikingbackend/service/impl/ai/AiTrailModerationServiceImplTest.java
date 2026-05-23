package com.sheng.hikingbackend.service.impl.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.config.AiProperties;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationDecision;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationPayload;

@ExtendWith(MockitoExtension.class)
class AiTrailModerationServiceImplTest {

    @Mock
    private DashScopeChatService dashScopeChatService;

    private AiTrailModerationServiceImpl moderationService;

    @BeforeEach
    void setUp() {
        AiProperties aiProperties = new AiProperties();
        aiProperties.setFastModel("qwen-plus");
        moderationService = new AiTrailModerationServiceImpl(dashScopeChatService, aiProperties, new ObjectMapper());
    }

    @Test
    void moderateTrailShouldParseApprovedDecision() {
        when(dashScopeChatService.completeJson(any(), eq("qwen-plus")))
                .thenReturn("{\"decision\":\"approved\",\"riskLevel\":\"low\",\"riskCategories\":[],\"reason\":\"未发现违规内容\"}");

        var result = moderationService.moderateTrail(buildPayload());

        assertEquals(AiTrailModerationDecision.APPROVED, result.decision());
        assertEquals("low", result.riskLevel());
        assertEquals("未发现违规内容", result.reason());
        assertEquals(false, result.fallbackByError());
    }

    @Test
    void moderateTrailShouldFallbackToManualReviewWhenProviderFails() {
        when(dashScopeChatService.completeJson(any(), eq("qwen-plus")))
                .thenThrow(new IllegalStateException("provider failed"));

        var result = moderationService.moderateTrail(buildPayload());

        assertEquals(AiTrailModerationDecision.NEEDS_MANUAL_REVIEW, result.decision());
        assertEquals("AI 审核服务异常，已转人工复核", result.reason());
        assertEquals(true, result.fallbackByError());
        assertEquals(List.of("credibility"), result.riskCategories());
    }

    private AiTrailModerationPayload buildPayload() {
        return AiTrailModerationPayload.builder()
                .trailId(1001L)
                .authorId(2001L)
                .sourceType("user_upload")
                .name("武功山轻装穿越")
                .location("江西 萍乡")
                .description("适合周末的新手路线")
                .difficulty("easy")
                .difficultyLabel("简单")
                .tags(List.of("新手", "云海"))
                .imageUrls(List.of("https://img.example.com/1.png"))
                .build();
    }
}
