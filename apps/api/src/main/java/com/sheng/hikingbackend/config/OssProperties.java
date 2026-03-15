package com.sheng.hikingbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage.oss")
public class OssProperties {

    private String region;
    private String bucketName;
    private String endpoint;
    private String publicUrlBase;
    private String accessKeyId;
    private String accessKeySecret;
    private String roleArn;
    private String roleSessionName = "trailquest-upload";

    @Min(900)
    @Max(3600)
    private Long stsDurationSeconds = 900L;
}
