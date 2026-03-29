package com.sheng.hikingbackend.service.impl.ai.model;

import java.util.List;

import com.sheng.hikingbackend.vo.ai.AiFollowUpVo;
import com.sheng.hikingbackend.vo.ai.AiTrailCardVo;

import lombok.Builder;

@Builder
public record AiMessageMetadata(List<AiTrailCardVo> trailCards, List<AiFollowUpVo> followUps) {
}
