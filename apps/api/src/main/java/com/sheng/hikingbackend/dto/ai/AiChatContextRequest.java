package com.sheng.hikingbackend.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatContextRequest {

    private Long currentTrailId;
    private String currentCity;
    private Boolean useUserPreference;
}
