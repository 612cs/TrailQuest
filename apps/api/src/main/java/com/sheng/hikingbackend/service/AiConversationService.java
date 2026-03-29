package com.sheng.hikingbackend.service;

import java.util.List;

import com.sheng.hikingbackend.entity.AiConversation;
import com.sheng.hikingbackend.entity.AiMessage;
import com.sheng.hikingbackend.vo.ai.AiConversationListItemVo;
import com.sheng.hikingbackend.vo.ai.AiMessageVo;

public interface AiConversationService {

    List<AiConversationListItemVo> listConversations(Long userId);

    AiConversation createConversation(Long userId, String title);

    AiConversation requireOwnedConversation(Long userId, Long conversationId);

    List<AiMessageVo> listMessages(Long userId, Long conversationId);

    List<AiMessage> listRecentMessages(Long conversationId, int limit);

    AiMessage appendUserMessage(Long conversationId, String content);

    AiMessage appendAssistantMessage(Long conversationId, String content, String metadataJson, Integer tokens);

    void touchConversation(Long conversationId);

    String buildConversationTitle(String firstMessage);
}
