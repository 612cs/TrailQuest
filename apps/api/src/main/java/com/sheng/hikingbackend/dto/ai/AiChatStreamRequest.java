package com.sheng.hikingbackend.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatStreamRequest {

    private Long conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息内容不能超过 4000 个字符")
    private String message;

    @Valid
    private AiChatContextRequest context;
}
