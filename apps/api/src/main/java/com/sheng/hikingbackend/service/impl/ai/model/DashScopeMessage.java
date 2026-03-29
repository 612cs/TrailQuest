package com.sheng.hikingbackend.service.impl.ai.model;

import lombok.Builder;

@Builder
public record DashScopeMessage(String role, String content) {
}
