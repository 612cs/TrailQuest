package com.sheng.hikingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiProperties {

    private String apiKey;
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private String model = "qwen3.5-plus";
    private String fastModel = "qwen-plus";
    private String reasoningModel = "qwen3.5-plus";
    private int connectTimeoutMs = 10000;
    private int readTimeoutMs = 120000;
}
