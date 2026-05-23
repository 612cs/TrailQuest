package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationPayload;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationResult;

public interface AiTrailModerationService {

    AiTrailModerationResult moderateTrail(AiTrailModerationPayload payload);
}
