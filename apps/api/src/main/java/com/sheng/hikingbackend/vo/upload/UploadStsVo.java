package com.sheng.hikingbackend.vo.upload;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadStsVo {

    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String region;
    private String bucket;
    private String endpoint;
    private String publicUrlBase;
    private String dir;
    private String objectKey;
    private Long expireAt;
}
