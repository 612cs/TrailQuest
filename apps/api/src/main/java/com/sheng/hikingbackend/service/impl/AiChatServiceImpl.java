package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.ai.AiChatStreamRequest;
import com.sheng.hikingbackend.entity.AiConversation;
import com.sheng.hikingbackend.entity.AiMessage;
import com.sheng.hikingbackend.service.AiChatService;
import com.sheng.hikingbackend.service.AiConversationService;
import com.sheng.hikingbackend.service.AiRouteRecommendationService;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.impl.ai.model.AiIntent;
import com.sheng.hikingbackend.service.impl.ai.model.AiMessageMetadata;
import com.sheng.hikingbackend.service.impl.ai.model.AiRecommendationResult;
import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiConversationService aiConversationService;
    private final AiRouteRecommendationService aiRouteRecommendationService;
    private final DashScopeChatService dashScopeChatService;
    private final ObjectMapper objectMapper;

    @Qualifier("aiTaskExecutor")
    private final Executor aiTaskExecutor;

    @Override
    public SseEmitter streamChat(Long userId, AiChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        aiTaskExecutor.execute(() -> handleStream(userId, request, emitter));
        return emitter;
    }

    private void handleStream(Long userId, AiChatStreamRequest request, SseEmitter emitter) {
        try {
            AiConversation conversation = resolveConversation(userId, request);
            aiConversationService.appendUserMessage(conversation.getId(), request.getMessage());
            sendEvent(emitter, "session", Map.of("conversationId", conversation.getId()));

            AiRecommendationResult recommendation = aiRouteRecommendationService.planRecommendation(userId, request.getMessage());
            if (!recommendation.trailCards().isEmpty()) {
                sendEvent(emitter, "trail_cards", recommendation.trailCards());
            }

            String metadataJson = objectMapper.writeValueAsString(AiMessageMetadata.builder()
                    .trailCards(recommendation.trailCards())
                    .followUps(recommendation.followUps())
                    .build());

            List<DashScopeMessage> promptMessages = buildPromptMessages(conversation.getId(), request, recommendation);
            String answer = dashScopeChatService.streamCompletion(promptMessages, delta ->
                    sendEventQuietly(emitter, "delta", Map.of("content", delta)));

            if (StringUtils.hasText(answer)) {
                aiConversationService.appendAssistantMessage(conversation.getId(), answer, metadataJson, null);
            }
            if (!recommendation.followUps().isEmpty()) {
                sendEvent(emitter, "follow_ups", recommendation.followUps());
            }

            Map<String, Object> donePayload = new LinkedHashMap<>();
            donePayload.put("conversationId", conversation.getId());
            donePayload.put("intent", recommendation.intent().name().toLowerCase());
            donePayload.put("finishedAt", LocalDateTime.now().toString());
            donePayload.put("hasTrailCards", !recommendation.trailCards().isEmpty());
            sendEvent(emitter, "done", donePayload);
            emitter.complete();
        } catch (Exception ex) {
            log.error("AI stream failed", ex);
            sendError(emitter, ex);
        }
    }

    private AiConversation resolveConversation(Long userId, AiChatStreamRequest request) {
        if (request.getConversationId() != null) {
            return aiConversationService.requireOwnedConversation(userId, request.getConversationId());
        }
        return aiConversationService.createConversation(userId, aiConversationService.buildConversationTitle(request.getMessage()));
    }

    private List<DashScopeMessage> buildPromptMessages(Long conversationId, AiChatStreamRequest request, AiRecommendationResult recommendation) {
        List<DashScopeMessage> messages = new ArrayList<>();
        messages.add(DashScopeMessage.builder()
                .role("system")
                .content(buildSystemPrompt())
                .build());
        messages.add(DashScopeMessage.builder()
                .role("system")
                .content("用户画像摘要：" + recommendation.preferenceSummary())
                .build());
        if (recommendation.intent() == AiIntent.TRAIL_RECOMMENDATION) {
            messages.add(DashScopeMessage.builder()
                    .role("system")
                    .content("路线检索结果：\n" + recommendation.routeFactsSummary()
                            + "\n要求：仅基于上述路线事实回答；如果没有命中路线，明确说明当前库内暂无完全匹配结果，并给出放宽条件的建议。")
                    .build());
        }
        for (AiMessage history : aiConversationService.listRecentMessages(conversationId, 10)) {
            if (!StringUtils.hasText(history.getContent())) {
                continue;
            }
            messages.add(DashScopeMessage.builder()
                    .role(history.getRole())
                    .content(history.getContent())
                    .build());
        }
        if (request.getContext() != null && StringUtils.hasText(request.getContext().getCurrentCity())) {
            messages.add(DashScopeMessage.builder()
                    .role("system")
                    .content("当前前端城市上下文：" + request.getContext().getCurrentCity())
                    .build());
        }
        return messages;
    }

    private String buildSystemPrompt() {
        return "你是 TrailQuest 的 AI 路线助手。语气专业、简洁、友好，优先帮助用户做徒步决策。\n"
                + "当用户在找路线时，先给结论，再用 2 到 4 句解释推荐理由。\n"
                + "如果系统已提供路线检索结果，只能基于这些路线回答，不能编造数据库中不存在的路线。\n"
                + "如果用户不是在找路线，也可以回答装备、天气、出行建议，但要尽量贴近徒步场景。\n"
                + "输出纯文本正文，不要输出 JSON、markdown 表格或代码块。";
    }

    private void sendError(SseEmitter emitter, Exception ex) {
        try {
            String message = ex instanceof BusinessException ? ex.getMessage() : "AI 对话暂时不可用，请稍后重试";
            String code = ex instanceof BusinessException businessException ? businessException.getCode() : "AI_STREAM_FAILED";
            sendEvent(emitter, "error", Map.of("code", code, "message", message));
            emitter.complete();
        } catch (Exception inner) {
            log.warn("Failed to send AI stream error event", inner);
            emitter.completeWithError(ex);
        }
    }

    private void sendEventQuietly(SseEmitter emitter, String event, Object payload) {
        try {
            sendEvent(emitter, event, payload);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void sendEvent(SseEmitter emitter, String event, Object payload) throws java.io.IOException {
        emitter.send(SseEmitter.event().name(event).data(payload));
    }
}
