package com.sheng.hikingbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 上传STS凭证信息
 * 存储在Redis中，用于校验前端上传的文件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadStsCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * OSS对象Key
     */
    private String objectKey;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 业务类型
     * AVATAR: 头像
     * COVER: 封面图
     * ALBUM: 相册图片
     * TRACK: 轨迹文件
     * REVIEW: 评论图片
     */
    private String bizType;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 存储区域
     */
    private String region;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 过期时间（秒）
     */
    private Long expirationSeconds;
}
