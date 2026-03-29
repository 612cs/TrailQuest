package com.sheng.hikingbackend.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sheng.hikingbackend.dto.ai.AiChatStreamRequest;

public interface AiChatService {

    SseEmitter streamChat(Long userId, AiChatStreamRequest request);
}
