package com.sheng.hikingbackend.vo.ai;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiMessageVo {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;
    private List<AiTrailCardVo> trailCards;
    private List<AiFollowUpVo> followUps;
}
