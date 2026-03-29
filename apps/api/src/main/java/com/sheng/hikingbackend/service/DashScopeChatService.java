package com.sheng.hikingbackend.service;

import java.util.List;
import java.util.function.Consumer;

import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;

public interface DashScopeChatService {

    String completeJson(List<DashScopeMessage> messages);

    String streamCompletion(List<DashScopeMessage> messages, Consumer<String> onDelta);
}
