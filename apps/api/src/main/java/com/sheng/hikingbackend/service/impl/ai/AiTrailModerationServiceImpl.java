package com.sheng.hikingbackend.service.impl.ai;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheng.hikingbackend.config.AiProperties;
import com.sheng.hikingbackend.service.AiTrailModerationService;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationDecision;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationPayload;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationResult;
import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTrailModerationServiceImpl implements AiTrailModerationService {

    private final DashScopeChatService dashScopeChatService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public AiTrailModerationResult moderateTrail(AiTrailModerationPayload payload) {
        try {
            List<DashScopeMessage> messages = List.of(
                    DashScopeMessage.builder()
                            .role("system")
                            .content(buildSystemPrompt())
                            .build(),
                    DashScopeMessage.builder()
                            .role("user")
                            .content(buildModerationInput(payload))
                            .build());
            String model = resolveModerationModel();
            String rawJson = dashScopeChatService.completeJson(messages, model);
            JsonNode root = objectMapper.readTree(rawJson);
            AiTrailModerationDecision decision = AiTrailModerationDecision.fromCode(root.path("decision").asText(null));
            if (decision == null) {
                return buildManualReviewResult(model, "AI 审核结果缺少有效结论", false);
            }
            return AiTrailModerationResult.builder()
                    .decision(decision)
                    .riskLevel(normalizeText(root.path("riskLevel").asText(null)))
                    .riskCategories(readStringArray(root.path("riskCategories")))
                    .reason(normalizeReason(root.path("reason").asText(null), decision))
                    .modelName(model)
                    .fallbackByError(false)
                    .build();
        } catch (Exception ex) {
            String model = resolveModerationModel();
            log.warn("AI trail moderation fallback to manual review trail_id={}", payload.trailId(), ex);
            return buildManualReviewResult(model, "AI 审核服务异常，已转人工复核", true);
        }
    }

    private String buildSystemPrompt() {
        return "你是 TrailQuest 的路线审核器。你只输出 JSON，不要输出 markdown。\n"
                + "固定输出结构：{\"decision\":\"approved|rejected|needs_manual_review\",\"riskLevel\":\"low|medium|high\",\"riskCategories\":[string],\"reason\":string}。\n"
                + "判定规则：\n"
                + "1. 涉及黄赌毒、暴力、自残、违法活动、广告引流、联系方式导流、敏感政治、明显垃圾内容时，返回 rejected。\n"
                + "2. 与徒步路线无关、信息真实性不足、地点明显不可信、描述疑似虚构、内容判断不稳定时，返回 needs_manual_review。\n"
                + "3. 只有当内容看起来是正常徒步路线且风险较低时，返回 approved。\n"
                + "4. 不确定时一律返回 needs_manual_review。\n"
                + "riskCategories 示例：porn、gambling、drug、violence、spam、illegal、irrelevant、credibility。\n"
                + "reason 用中文，控制在 40 字以内。";
    }

    private String buildModerationInput(AiTrailModerationPayload payload) throws Exception {
        ObjectNode node = objectMapper.createObjectNode()
                .put("trailId", payload.trailId())
                .put("authorId", payload.authorId())
                .put("sourceType", defaultText(payload.sourceType(), "user_upload"))
                .put("name", defaultText(payload.name(), ""))
                .put("location", defaultText(payload.location(), ""))
                .put("description", defaultText(payload.description(), ""))
                .put("difficulty", defaultText(payload.difficulty(), ""))
                .put("difficultyLabel", defaultText(payload.difficultyLabel(), ""));
        node.set("tags", objectMapper.valueToTree(payload.tags() == null ? List.of() : payload.tags()));
        node.set("imageUrls", objectMapper.valueToTree(payload.imageUrls() == null ? List.of() : payload.imageUrls()));
        return objectMapper.writeValueAsString(node);
    }

    private AiTrailModerationResult buildManualReviewResult(String model, String reason, boolean fallbackByError) {
        return AiTrailModerationResult.builder()
                .decision(AiTrailModerationDecision.NEEDS_MANUAL_REVIEW)
                .riskLevel("medium")
                .riskCategories(List.of("credibility"))
                .reason(reason)
                .modelName(model)
                .fallbackByError(fallbackByError)
                .build();
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        return java.util.stream.StreamSupport.stream(node.spliterator(), false)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .map(this::normalizeText)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalizeReason(String reason, AiTrailModerationDecision decision) {
        String normalized = normalizeText(reason);
        if (StringUtils.hasText(normalized)) {
            return normalized;
        }
        return switch (decision) {
            case APPROVED -> "AI 预审通过";
            case REJECTED -> "AI 预审拒绝";
            case NEEDS_MANUAL_REVIEW -> "AI 建议人工复核";
        };
    }

    private String resolveModerationModel() {
        if (StringUtils.hasText(aiProperties.getFastModel())) {
            return aiProperties.getFastModel().trim();
        }
        return StringUtils.hasText(aiProperties.getModel()) ? aiProperties.getModel().trim() : "qwen3.5-plus";
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
