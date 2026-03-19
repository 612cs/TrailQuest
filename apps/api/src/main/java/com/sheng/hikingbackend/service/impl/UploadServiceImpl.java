package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sheng.hikingbackend.common.enums.MediaBizType;
import com.sheng.hikingbackend.common.enums.MediaFileStatus;
import com.sheng.hikingbackend.common.enums.StorageProvider;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.config.OssProperties;
import com.sheng.hikingbackend.dto.upload.CompleteUploadRequest;
import com.sheng.hikingbackend.dto.upload.CreateUploadStsRequest;
import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.mapper.MediaFileMapper;
import com.sheng.hikingbackend.service.UploadService;
import com.sheng.hikingbackend.vo.upload.MediaFileVo;
import com.sheng.hikingbackend.vo.upload.UploadStsVo;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final String STS_CACHE_PREFIX = "upload:sts:";

    private final OssProperties ossProperties;
    private final MediaFileMapper mediaFileMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UploadStsVo createUploadSts(Long userId, CreateUploadStsRequest request) {
        validateOssProperties();

        MediaBizType bizType = requireBizType(request.getBizType());
        validateImageFile(request.getFileName(), request.getMimeType());

        String extension = extractExtension(request.getFileName());
        String objectKey = buildObjectKey(bizType, userId, extension);
        String dir = objectKey.substring(0, objectKey.lastIndexOf('/') + 1);
        long expireAt = System.currentTimeMillis() + ossProperties.getStsDurationSeconds() * 1000;

        cacheIssuedObjectKey(userId, bizType, objectKey);

        AssumeRoleResponse.Credentials credentials = assumeRole(objectKey);
        return UploadStsVo.builder()
                .accessKeyId(credentials.getAccessKeyId())
                .accessKeySecret(credentials.getAccessKeySecret())
                .securityToken(credentials.getSecurityToken())
                .region(ossProperties.getRegion())
                .bucket(ossProperties.getBucketName())
                .endpoint(ossProperties.getEndpoint())
                .publicUrlBase(resolvePublicUrlBase())
                .dir(dir)
                .objectKey(objectKey)
                .expireAt(expireAt)
                .build();
    }

    @Override
    public MediaFileVo completeUpload(Long userId, CompleteUploadRequest request) {
        validateOssProperties();

        MediaBizType bizType = requireBizType(request.getBizType());
        validateImageFile(request.getOriginalName(), request.getMimeType());
        validateUploadMetadata(request);
        validateIssuedObjectKey(userId, bizType, request.getObjectKey());
        validateUrl(request.getUrl(), request.getObjectKey());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setUserId(userId);
        mediaFile.setStorageProvider(StorageProvider.ALIYUN_OSS.getCode());
        mediaFile.setBucketName(ossProperties.getBucketName());
        mediaFile.setObjectKey(request.getObjectKey());
        mediaFile.setUrl(request.getUrl());
        mediaFile.setBizType(bizType.getCode());
        mediaFile.setOriginalName(normalizeBlank(request.getOriginalName()));
        mediaFile.setExtension(resolveExtension(request));
        mediaFile.setMimeType(request.getMimeType());
        mediaFile.setSize(request.getSize());
        mediaFile.setWidth(request.getWidth());
        mediaFile.setHeight(request.getHeight());
        mediaFile.setStatus(MediaFileStatus.ACTIVE.getCode());

        try {
            mediaFileMapper.insert(mediaFile);
        } catch (DuplicateKeyException ex) {
            MediaFile existing = mediaFileMapper.selectOne(new LambdaQueryWrapper<MediaFile>()
                    .eq(MediaFile::getBucketName, ossProperties.getBucketName())
                    .eq(MediaFile::getObjectKey, request.getObjectKey())
                    .last("LIMIT 1"));
            if (existing != null && existing.getUserId().equals(userId)) {
                return MediaFileVo.from(existing);
            }
            throw BusinessException.badRequest("MEDIA_FILE_ALREADY_EXISTS", "该文件已存在，请重新上传");
        }

        clearIssuedObjectKey(request.getObjectKey());
        return MediaFileVo.from(mediaFile);
    }

    @Override
    public String resolveAvatarUrl(Long mediaFileId) {
        if (mediaFileId == null) {
            return null;
        }
        MediaFile mediaFile = mediaFileMapper.selectById(mediaFileId);
        if (mediaFile == null || !MediaFileStatus.ACTIVE.getCode().equals(mediaFile.getStatus())) {
            return null;
        }
        return mediaFile.getUrl();
    }

    private void validateOssProperties() {
        if (!StringUtils.hasText(ossProperties.getRegion())
                || !StringUtils.hasText(ossProperties.getBucketName())
                || !StringUtils.hasText(ossProperties.getEndpoint())
                || !StringUtils.hasText(ossProperties.getAccessKeyId())
                || !StringUtils.hasText(ossProperties.getAccessKeySecret())
                || !StringUtils.hasText(ossProperties.getRoleArn())) {
            throw BusinessException.badRequest("OSS_CONFIG_MISSING", "OSS 配置不完整，请检查后端配置");
        }
    }

    private MediaBizType requireBizType(String bizTypeCode) {
        MediaBizType bizType = MediaBizType.fromCode(bizTypeCode);
        if (bizType == null) {
            throw BusinessException.badRequest("INVALID_BIZ_TYPE", "不支持的业务类型");
        }
        return bizType;
    }

    private void validateImageFile(String fileName, String mimeType) {
        if (!StringUtils.hasText(mimeType) || !mimeType.startsWith("image/")) {
            throw BusinessException.badRequest("INVALID_FILE_TYPE", "仅支持图片上传");
        }
        if (!("image/jpeg".equals(mimeType) || "image/png".equals(mimeType) || "image/webp".equals(mimeType))) {
            throw BusinessException.badRequest("INVALID_FILE_TYPE", "仅支持 JPG、PNG、WEBP 图片");
        }
        if (fileName != null && fileName.contains("..")) {
            throw BusinessException.badRequest("INVALID_FILE_NAME", "文件名不合法");
        }
    }

    private void validateUploadMetadata(CompleteUploadRequest request) {
        if (request.getSize() > MAX_IMAGE_SIZE) {
            throw BusinessException.badRequest("FILE_TOO_LARGE", "图片大小不能超过 10MB");
        }
        if (request.getWidth() != null && request.getWidth() <= 0) {
            throw BusinessException.badRequest("INVALID_IMAGE_WIDTH", "图片宽度不合法");
        }
        if (request.getHeight() != null && request.getHeight() <= 0) {
            throw BusinessException.badRequest("INVALID_IMAGE_HEIGHT", "图片高度不合法");
        }
    }

    private void validateIssuedObjectKey(Long userId, MediaBizType bizType, String objectKey) {
        String cacheValue = stringRedisTemplate.opsForValue().get(buildCacheKey(objectKey));
        if (cacheValue == null) {
            throw BusinessException.badRequest("UPLOAD_SESSION_EXPIRED", "上传凭证已失效，请重新获取");
        }

        String expectedValue = userId + ":" + bizType.getCode();
        if (!expectedValue.equals(cacheValue)) {
            throw BusinessException.badRequest("UPLOAD_SESSION_MISMATCH", "上传会话校验失败");
        }
    }

    private void validateUrl(String url, String objectKey) {
        String expectedUrl = joinUrl(resolvePublicUrlBase(), objectKey);
        if (!expectedUrl.equals(url)) {
            throw BusinessException.badRequest("INVALID_FILE_URL", "文件访问地址不合法");
        }
    }

    private void cacheIssuedObjectKey(Long userId, MediaBizType bizType, String objectKey) {
        stringRedisTemplate.opsForValue().set(
                buildCacheKey(objectKey),
                userId + ":" + bizType.getCode(),
                ossProperties.getStsDurationSeconds(),
                TimeUnit.SECONDS);
    }

    private void clearIssuedObjectKey(String objectKey) {
        stringRedisTemplate.delete(buildCacheKey(objectKey));
    }

    private String buildCacheKey(String objectKey) {
        return STS_CACHE_PREFIX + objectKey;
    }

    private AssumeRoleResponse.Credentials assumeRole(String objectKey) {
        try {
            IClientProfile profile = DefaultProfile.getProfile(
                    ossProperties.getRegion(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(ossProperties.getRoleArn());
            request.setRoleSessionName(ossProperties.getRoleSessionName());
            request.setDurationSeconds(ossProperties.getStsDurationSeconds());
            request.setPolicy(buildUploadPolicy(objectKey));

            return client.getAcsResponse(request).getCredentials();
        } catch (ClientException ex) {
            log.error(
                    "Failed to assume OSS role. errCode={}, errMsg={}, requestId={}, roleArn={}, bucket={}, region={}",
                    ex.getErrCode(),
                    ex.getErrMsg(),
                    ex.getRequestId(),
                    ossProperties.getRoleArn(),
                    ossProperties.getBucketName(),
                    ossProperties.getRegion(),
                    ex);
            throw BusinessException.badRequest("OSS_STS_REQUEST_FAILED", "获取 OSS 临时凭证失败");
        }
    }

    private String buildUploadPolicy(String objectKey) {
        String resource = "acs:oss:*:*:" + ossProperties.getBucketName() + "/" + objectKey;

        Map<String, Object> statement = new LinkedHashMap<>();
        statement.put("Effect", "Allow");
        statement.put("Action", new String[] {
                "oss:PutObject",
                "oss:AbortMultipartUpload",
                "oss:InitiateMultipartUpload",
                "oss:UploadPart",
                "oss:CompleteMultipartUpload"
        });
        statement.put("Resource", new String[] { resource });

        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("Version", "1");
        policy.put("Statement", new Object[] { statement });

        return toJson(policy);
    }

    private String buildObjectKey(MediaBizType bizType, Long userId, String extension) {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return "%s/%d/%d/%02d/%s.%s".formatted(
                bizType.getCode(),
                userId,
                now.getYear(),
                now.getMonthValue(),
                suffix,
                extension);
    }

    private String resolvePublicUrlBase() {
        if (StringUtils.hasText(ossProperties.getPublicUrlBase())) {
            return trimTrailingSlash(ossProperties.getPublicUrlBase());
        }
        return "https://%s.%s".formatted(ossProperties.getBucketName(), trimProtocol(ossProperties.getEndpoint()));
    }

    private String trimProtocol(String endpoint) {
        return endpoint.replaceFirst("^https?://", "");
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String joinUrl(String base, String objectKey) {
        return trimTrailingSlash(base) + "/" + objectKey;
    }

    private String resolveExtension(CompleteUploadRequest request) {
        if (StringUtils.hasText(request.getExtension())) {
            return request.getExtension().trim().toLowerCase();
        }
        return extractExtension(request.getOriginalName());
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            throw BusinessException.badRequest("INVALID_FILE_EXTENSION", "文件扩展名不合法");
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase();
        if (!StringUtils.hasText(extension)) {
            throw BusinessException.badRequest("INVALID_FILE_EXTENSION", "文件扩展名不合法");
        }
        return extension;
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String toJson(Map<String, Object> payload) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"Version\":\"").append(payload.get("Version")).append("\",\"Statement\":[");
        Object[] statements = (Object[]) payload.get("Statement");
        for (int i = 0; i < statements.length; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> statement = (Map<String, Object>) statements[i];
            if (i > 0) {
                builder.append(",");
            }
            builder.append("{\"Effect\":\"").append(statement.get("Effect")).append("\",");
            builder.append("\"Action\":[");
            String[] actions = (String[]) statement.get("Action");
            for (int j = 0; j < actions.length; j++) {
                if (j > 0) {
                    builder.append(",");
                }
                builder.append("\"").append(actions[j]).append("\"");
            }
            builder.append("],\"Resource\":[");
            String[] resources = (String[]) statement.get("Resource");
            for (int j = 0; j < resources.length; j++) {
                if (j > 0) {
                    builder.append(",");
                }
                builder.append("\"").append(resources[j]).append("\"");
            }
            builder.append("]}");
        }
        builder.append("]}");
        return builder.toString();
    }
}
