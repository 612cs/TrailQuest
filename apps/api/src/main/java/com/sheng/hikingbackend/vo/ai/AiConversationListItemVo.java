package com.sheng.hikingbackend.vo.ai;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiConversationListItemVo {

    private Long id;
    private String title;
    private LocalDateTime updatedAt;
    private String preview;
}
