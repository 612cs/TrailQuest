package com.sheng.hikingbackend.service.impl;

import com.sheng.hikingbackend.dto.UploadStsCredential;
import com.sheng.hikingbackend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 上传服务实现类（示例）
 * 展示如何使用Redis进行上传凭证管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadServiceWithRedisExample {

    private final RedisCacheService redisCacheService;

    /**
     * 签发OSS STS临时上传凭证
     *
     * 流程：
     * 1. 生成唯一的objectKey
     * 2. 调用OSS SDK获取STS临时凭证（这里模拟）
     * 3. 将凭证信息存入Redis，设置过期时间
     * 4. 返回凭证给前端
     */
    public void generateUploadCredentialExample(Long userId, String bizType) {

        // 1. 生成唯一的objectKey
        String objectKey = generateObjectKey(userId, bizType);

        // 2. 模拟调用OSS SDK获取STS凭证
        // 实际项目中这里会调用阿里云OSS SDK
        String accessKeyId = "STS.mock.access.key.id";
        String accessKeySecret = "mock.access.key.secret";
        String securityToken = "mock.security.token";
        String url = "https://hiking-oss.oss-cn-hangzhou.aliyuncs.com/" + objectKey;

        // 3. 构造凭证信息
        UploadStsCredential credential = UploadStsCredential.builder()
            .objectKey(objectKey)
            .userId(userId)
            .bizType(bizType)
            .url(url)
            .bucketName("hiking-oss")
            .region("oss-cn-hangzhou")
            .createdAt(LocalDateTime.now())
            .expirationSeconds(3600L)
            .build();

        // 4. 存入Redis，过期时间3600秒（1小时）
        redisCacheService.saveUploadCredential(objectKey, credential, 3600L);

        log.info("签发上传凭证成功: userId={}, objectKey={}, bizType={}", userId, objectKey, bizType);

        // 5. 返回给前端（这里只是示例，实际会封装成VO返回）
        // return OssUploadCredentialVo.builder()
        //     .accessKeyId(accessKeyId)
        //     .accessKeySecret(accessKeySecret)
        //     .securityToken(securityToken)
        //     .objectKey(objectKey)
        //     .bucket("hiking-oss")
        //     .region("oss-cn-hangzhou")
        //     .expiration(LocalDateTime.now().plusHours(1))
        //     .build();
    }

    /**
     * 确认上传完成，校验凭证并写入数据库
     *
     * 流程：
     * 1. 从Redis获取凭证信息
     * 2. 校验凭证是否存在、用户ID是否匹配、业务类型是否匹配
     * 3. 校验文件是否真实存在于OSS（这里模拟）
     * 4. 写入数据库
     * 5. 立即删除Redis凭证，防止重复使用
     */
    public void confirmUploadExample(Long userId, String objectKey, String bizType) {

        // 1. 校验凭证（内部会从Redis获取并校验）
        UploadStsCredential credential = redisCacheService.validateUploadCredential(
            objectKey, userId, bizType
        );

        // 2. 模拟校验文件是否存在于OSS
        // 实际项目中这里会调用OSS SDK的headObject方法
        boolean fileExists = true; // 模拟文件存在
        if (!fileExists) {
            throw new RuntimeException("文件不存在，请重新上传");
        }

        // 3. 写入数据库（这里模拟）
        log.info("写入媒体文件到数据库: objectKey={}, url={}", objectKey, credential.getUrl());
        // mediaFileMapper.insert(mediaFile);

        // 4. 立即删除Redis凭证，防止重复使用
        redisCacheService.deleteUploadCredential(objectKey);

        log.info("上传确认成功: userId={}, objectKey={}", userId, objectKey);
    }

    /**
     * 生成唯一的objectKey
     */
    private String generateObjectKey(Long userId, String bizType) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return String.format("%s/%s/%s_%s.jpg",
                           bizType.toLowerCase(),
                           userId,
                           timestamp,
                           uuid);
    }
}
