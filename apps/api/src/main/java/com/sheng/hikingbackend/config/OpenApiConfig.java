package com.sheng.hikingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trailQuestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrailQuest API")
                        .description("TrailQuest hiking application backend APIs")
                        .version("v1"));
    }
}
