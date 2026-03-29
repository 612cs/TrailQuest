package com.sheng.hikingbackend.dto.ai;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAiConversationRequest {

    @Size(max = 100, message = "会话标题不能超过 100 个字符")
    private String title;
}
