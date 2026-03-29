package com.sheng.hikingbackend.websocket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.config.AiProperties;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.config.CustomUserDetailsService;
import com.sheng.hikingbackend.config.JwtTokenProvider;
import com.sheng.hikingbackend.dto.ai.AiChatContextRequest;
import com.sheng.hikingbackend.dto.ai.AiChatStreamRequest;
import com.sheng.hikingbackend.entity.AiConversation;
import com.sheng.hikingbackend.entity.AiMessage;
import com.sheng.hikingbackend.service.AiConversationService;
import com.sheng.hikingbackend.service.AiRouteRecommendationService;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.impl.ai.model.AiIntent;
import com.sheng.hikingbackend.service.impl.ai.model.AiMessageMetadata;
import com.sheng.hikingbackend.service.impl.ai.model.AiRecommendationResult;
import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AiWebSocketHandler extends TextWebSocketHandler {

    private static final String USER_ID_KEY = "userId";

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final AiConversationService aiConversationService;
    private final AiRouteRecommendationService aiRouteRecommendationService;
    private final DashScopeChatService dashScopeChatService;
    private final AiProperties aiProperties;
    private final Executor aiTaskExecutor;

    public AiWebSocketHandler(
            ObjectMapper objectMapper,
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService,
            AiConversationService aiConversationService,
            AiRouteRecommendationService aiRouteRecommendationService,
            DashScopeChatService dashScopeChatService,
            AiProperties aiProperties,
            @Qualifier("aiTaskExecutor") Executor aiTaskExecutor) {
        this.objectMapper = objectMapper;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.aiConversationService = aiConversationService;
        this.aiRouteRecommendationService = aiRouteRecommendationService;
        this.dashScopeChatService = dashScopeChatService;
        this.aiProperties = aiProperties;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.path("type").asText("");
        switch (type) {
            case "auth" -> handleAuth(session, payload);
            case "chat.send" -> handleChatSend(session, payload);
            default -> sendEvent(session, "error", Map.of("code", "AI_WS_UNSUPPORTED", "message", "不支持的消息类型"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("AI websocket closed session_id={} code={}", session.getId(), status.getCode());
    }

    private void handleAuth(WebSocketSession session, JsonNode payload) throws Exception {
        String token = payload.path("token").asText("");
        if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
            sendEvent(session, "error", Map.of("code", "UNAUTHORIZED", "message", "登录态已失效，请重新登录"));
            return;
        }
        String email = jwtTokenProvider.getSubject(token);
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);
        session.getAttributes().put(USER_ID_KEY, userDetails.getId());
        sendEvent(session, "auth.ok", Map.of("userId", userDetails.getId(), "displayName", userDetails.getDisplayName()));
    }

    private void handleChatSend(WebSocketSession session, JsonNode payload) throws Exception {
        Long userId = (Long) session.getAttributes().get(USER_ID_KEY);
        if (userId == null) {
            sendEvent(session, "error", Map.of("code", "UNAUTHORIZED", "message", "请先完成登录认证"));
            return;
        }

        AiChatStreamRequest request = new AiChatStreamRequest();
        if (payload.hasNonNull("conversationId")) {
            request.setConversationId(payload.path("conversationId").asLong());
        }
        request.setMessage(payload.path("message").asText(""));
        if (!StringUtils.hasText(request.getMessage())) {
            sendEvent(session, "error", Map.of("code", "AI_MESSAGE_REQUIRED", "message", "消息内容不能为空"));
            return;
        }

        if (payload.has("context") && !payload.path("context").isNull()) {
            request.setContext(objectMapper.treeToValue(payload.path("context"), AiChatContextRequest.class));
        }

        aiTaskExecutor.execute(() -> handleChat(session, userId, request));
    }

    private void handleChat(WebSocketSession session, Long userId, AiChatStreamRequest request) {
        try {
            AiConversation conversation = resolveConversation(userId, request);
            aiConversationService.appendUserMessage(conversation.getId(), request.getMessage());
            sendEvent(session, "session", Map.of("conversationId", conversation.getId()));

            long recommendationStartedAt = System.currentTimeMillis();
            AiRecommendationResult recommendation = aiRouteRecommendationService.planRecommendation(userId, request.getMessage());
            long recommendationMs = System.currentTimeMillis() - recommendationStartedAt;

            if (!recommendation.trailCards().isEmpty()) {
                sendEvent(session, "trail_cards", recommendation.trailCards());
            }

            String metadataJson = objectMapper.writeValueAsString(AiMessageMetadata.builder()
                    .trailCards(recommendation.trailCards())
                    .followUps(recommendation.followUps())
                    .build());

            List<DashScopeMessage> promptMessages = buildPromptMessages(conversation.getId(), request, recommendation);
            long modelStartedAt = System.currentTimeMillis();
            long[] firstTokenMs = new long[] { -1L };
            String answer = dashScopeChatService.streamCompletion(
                    promptMessages,
                    resolveChatModel(recommendation.intent()),
                    delta -> {
                        if (firstTokenMs[0] < 0) {
                            firstTokenMs[0] = System.currentTimeMillis() - modelStartedAt;
                        }
                        sendEvent(session, "delta", Map.of("content", delta));
                    });
            long modelTotalMs = System.currentTimeMillis() - modelStartedAt;

            if (StringUtils.hasText(answer)) {
                aiConversationService.appendAssistantMessage(conversation.getId(), answer, metadataJson, null);
            }
            if (!recommendation.followUps().isEmpty()) {
                sendEvent(session, "follow_ups", recommendation.followUps());
            }

            sendEvent(session, "done", Map.of(
                    "conversationId", conversation.getId(),
                    "intent", recommendation.intent().name().toLowerCase(),
                    "finishedAt", LocalDateTime.now().toString(),
                    "hasTrailCards", !recommendation.trailCards().isEmpty(),
                    "intentParseMs", recommendationMs,
                    "modelFirstTokenMs", firstTokenMs[0],
                    "modelTotalMs", modelTotalMs));

            log.info(
                    "AI websocket completed conversation_id={} intent={} recommendation_ms={} model_first_token_ms={} model_total_ms={} trail_cards={}",
                    conversation.getId(),
                    recommendation.intent().name().toLowerCase(),
                    recommendationMs,
                    firstTokenMs[0],
                    modelTotalMs,
                    recommendation.trailCards().size());
        } catch (Exception ex) {
            log.error("AI websocket chat failed", ex);
            sendError(session, ex);
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

    private String resolveChatModel(AiIntent intent) {
        if (intent == AiIntent.TRAIL_RECOMMENDATION) {
            return StringUtils.hasText(aiProperties.getReasoningModel())
                    ? aiProperties.getReasoningModel()
                    : aiProperties.getModel();
        }
        return StringUtils.hasText(aiProperties.getFastModel())
                ? aiProperties.getFastModel()
                : aiProperties.getModel();
    }

    private void sendError(WebSocketSession session, Exception ex) {
        String message = ex instanceof BusinessException ? ex.getMessage() : "AI 对话暂时不可用，请稍后重试";
        String code = ex instanceof BusinessException businessException ? businessException.getCode() : "AI_STREAM_FAILED";
        sendEvent(session, "error", Map.of("code", code, "message", message));
    }

    private void sendEvent(WebSocketSession session, String type, Object data) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of("type", type, "data", data));
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
