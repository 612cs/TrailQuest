package com.sheng.hikingbackend.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.entity.AiConversation;
import com.sheng.hikingbackend.entity.AiMessage;
import com.sheng.hikingbackend.mapper.AiConversationMapper;
import com.sheng.hikingbackend.mapper.AiMessageMapper;
import com.sheng.hikingbackend.service.AiConversationService;
import com.sheng.hikingbackend.service.impl.ai.model.AiMessageMetadata;
import com.sheng.hikingbackend.vo.ai.AiConversationListItemVo;
import com.sheng.hikingbackend.vo.ai.AiMessageVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationMapper aiConversationMapper;
    private final AiMessageMapper aiMessageMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiConversationListItemVo> listConversations(Long userId) {
        return aiConversationMapper.selectActiveByUserId(userId).stream()
                .map(conversation -> AiConversationListItemVo.builder()
                        .id(conversation.getId())
                        .title(conversation.getTitle())
                        .updatedAt(conversation.getUpdatedAt())
                        .preview(buildPreview(aiMessageMapper.selectLatestContent(conversation.getId())))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public AiConversation createConversation(Long userId, String title) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle(StringUtils.hasText(title) ? title.trim() : "新的对话");
        conversation.setModel("qwen3.5-plus");
        conversation.setStatus("active");
        aiConversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public AiConversation requireOwnedConversation(Long userId, Long conversationId) {
        AiConversation conversation = aiConversationMapper.selectOwnedConversation(conversationId, userId);
        if (conversation == null) {
            throw BusinessException.notFound("AI_CONVERSATION_NOT_FOUND", "会话不存在或无权访问");
        }
        return conversation;
    }

    @Override
    public List<AiMessageVo> listMessages(Long userId, Long conversationId) {
        requireOwnedConversation(userId, conversationId);
        return aiMessageMapper.selectByConversationId(conversationId).stream()
                .sorted(Comparator.comparing(AiMessage::getCreatedAt).thenComparing(AiMessage::getId))
                .map(this::toMessageVo)
                .toList();
    }

    @Override
    public List<AiMessage> listRecentMessages(Long conversationId, int limit) {
        List<AiMessage> messages = aiMessageMapper.selectLatestByConversationId(conversationId, limit);
        Collections.reverse(messages);
        return messages;
    }

    @Override
    @Transactional
    public AiMessage appendUserMessage(Long conversationId, String content) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole("user");
        message.setContent(content.trim());
        message.setTokens(estimateTokens(content));
        aiMessageMapper.insert(message);
        touchConversation(conversationId);
        return message;
    }

    @Override
    @Transactional
    public AiMessage appendAssistantMessage(Long conversationId, String content, String metadataJson, Integer tokens) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole("assistant");
        message.setContent(content);
        message.setMetadataJson(metadataJson);
        message.setTokens(tokens == null ? estimateTokens(content) : tokens);
        aiMessageMapper.insert(message);
        touchConversation(conversationId);
        return message;
    }

    @Override
    public void touchConversation(Long conversationId) {
        AiConversation conversation = new AiConversation();
        conversation.setId(conversationId);
        aiConversationMapper.updateById(conversation);
    }

    @Override
    public String buildConversationTitle(String firstMessage) {
        String trimmed = firstMessage == null ? "" : firstMessage.trim();
        if (!StringUtils.hasText(trimmed)) {
            return "新的对话";
        }
        return trimmed.length() <= 18 ? trimmed : trimmed.substring(0, 18) + "...";
    }

    private AiMessageVo toMessageVo(AiMessage message) {
        AiMessageMetadata metadata = parseMetadata(message.getMetadataJson());
        return AiMessageVo.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .trailCards(metadata.trailCards())
                .followUps(metadata.followUps())
                .build();
    }

    private AiMessageMetadata parseMetadata(String metadataJson) {
        if (!StringUtils.hasText(metadataJson)) {
            return AiMessageMetadata.builder()
                    .trailCards(List.of())
                    .followUps(List.of())
                    .build();
        }
        try {
            return objectMapper.readValue(metadataJson, AiMessageMetadata.class);
        } catch (Exception ex) {
            log.warn("Failed to parse AI message metadata", ex);
            return AiMessageMetadata.builder()
                    .trailCards(List.of())
                    .followUps(List.of())
                    .build();
        }
    }

    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "开始新的路线对话";
        }
        String trimmed = content.trim().replace('\n', ' ');
        return trimmed.length() <= 40 ? trimmed : trimmed.substring(0, 40) + "...";
    }

    private int estimateTokens(String content) {
        if (!StringUtils.hasText(content)) {
            return 0;
        }
        return Math.max(1, content.trim().length() / 2);
    }
}
