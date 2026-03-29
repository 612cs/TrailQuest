package com.sheng.hikingbackend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.ai.AiChatStreamRequest;
import com.sheng.hikingbackend.dto.ai.CreateAiConversationRequest;
import com.sheng.hikingbackend.entity.AiConversation;
import com.sheng.hikingbackend.service.AiChatService;
import com.sheng.hikingbackend.service.AiConversationService;
import com.sheng.hikingbackend.vo.ai.AiConversationListItemVo;
import com.sheng.hikingbackend.vo.ai.AiMessageVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiConversationService aiConversationService;
    private final AiChatService aiChatService;

    @GetMapping("/conversations")
    public ApiResponse<List<AiConversationListItemVo>> listConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success("AI 会话加载成功", aiConversationService.listConversations(userDetails.getId()));
    }

    @PostMapping("/conversations")
    public ApiResponse<AiConversationListItemVo> createConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody(required = false) CreateAiConversationRequest request) {
        AiConversation conversation = aiConversationService.createConversation(
                userDetails.getId(),
                request == null ? null : request.getTitle());
        return ApiResponse.success("AI 会话创建成功", AiConversationListItemVo.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .updatedAt(conversation.getUpdatedAt())
                .preview("开始新的路线对话")
                .build());
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<AiMessageVo>> listMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success("AI 会话消息加载成功", aiConversationService.listMessages(userDetails.getId(), id));
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        aiConversationService.deleteConversation(userDetails.getId(), id);
        return ApiResponse.success("AI 会话删除成功", null);
    }

    @PostMapping(path = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AiChatStreamRequest request) {
        return aiChatService.streamChat(userDetails.getId(), request);
    }
}
