package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.dto.UploadStsCredential;
import com.sheng.hikingbackend.vo.MediaFileVo;
import com.sheng.hikingbackend.vo.OssUploadCredentialVo;

/**
 * Redis缓存服务接口
 * 提供Redis相关的缓存操作
 */
public interface RedisCacheService {

    // ==================== 上传凭证相关 ====================

    /**
     * 保存上传凭证到Redis
     * @param objectKey OSS对象Key
     * @param credential 凭证信息
     * @param expireSeconds 过期时间（秒）
     */
    void saveUploadCredential(String objectKey, UploadStsCredential credential, long expireSeconds);

    /**
     * 获取上传凭证
     * @param objectKey OSS对象Key
     * @return 凭证信息，不存在返回null
     */
    UploadStsCredential getUploadCredential(String objectKey);

    /**
     * 删除上传凭证
     * @param objectKey OSS对象Key
     */
    void deleteUploadCredential(String objectKey);

    /**
     * 校验上传凭证
     * @param objectKey OSS对象Key
     * @param userId 用户ID
     * @param bizType 业务类型
     * @return 凭证信息
     * @throws RuntimeException 凭证不存在、已过期或校验失败
     */
    UploadStsCredential validateUploadCredential(String objectKey, Long userId, String bizType);

    // ==================== 天气缓存相关 ====================

    /**
     * 缓存路线天气信息
     * @param trailId 路线ID
     * @param weatherData 天气数据
     * @param expireSeconds 过期时间（秒）
     */
    void cacheTrailWeather(Long trailId, Object weatherData, long expireSeconds);

    /**
     * 获取路线天气缓存
     * @param trailId 路线ID
     * @return 天气数据，不存在返回null
     */
    Object getTrailWeatherCache(Long trailId);

    // ==================== 景观预测缓存相关 ====================

    /**
     * 缓存景观预测结果
     * @param trailId 路线ID
     * @param date 预测日期
     * @param predictionData 预测数据
     * @param expireSeconds 过期时间（秒）
     */
    void cacheLandscapePrediction(Long trailId, String date, Object predictionData, long expireSeconds);

    /**
     * 获取景观预测缓存
     * @param trailId 路线ID
     * @param date 预测日期
     * @return 预测数据，不存在返回null
     */
    Object getLandscapePredictionCache(Long trailId, String date);

    // ==================== AI会话缓存相关 ====================

    /**
     * 缓存AI会话上下文
     * @param userId 用户ID
     * @param contextData 上下文数据
     * @param expireSeconds 过期时间（秒）
     */
    void cacheAiConversationContext(Long userId, Object contextData, long expireSeconds);

    /**
     * 获取AI会话上下文缓存
     * @param userId 用户ID
     * @return 上下文数据，不存在返回null
     */
    Object getAiConversationContextCache(Long userId);

    /**
     * 清除AI会话上下文缓存
     * @param userId 用户ID
     */
    void invalidateAiConversationCache(Long userId);

    // ==================== 热门路线缓存相关 ====================

    /**
     * 缓存热门路线列表
     * @param trails 路线列表
     * @param expireSeconds 过期时间（秒）
     */
    void cacheHotTrails(Object trails, long expireSeconds);

    /**
     * 获取热门路线缓存
     * @return 路线列表，不存在返回null
     */
    Object getHotTrailsCache();
}
