package com.sheng.hikingbackend.service.impl;

import com.sheng.hikingbackend.constant.RedisKeyConstants;
import com.sheng.hikingbackend.dto.UploadStsCredential;
import com.sheng.hikingbackend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现类
 * 封装Redis操作，提供统一的缓存接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== 上传凭证相关 ====================

    @Override
    public void saveUploadCredential(String objectKey, UploadStsCredential credential, long expireSeconds) {
        String redisKey = RedisKeyConstants.uploadStsKey(objectKey);
        redisTemplate.opsForValue().set(redisKey, credential, expireSeconds, TimeUnit.SECONDS);
        log.info("保存上传凭证到Redis: objectKey={}, userId={}, bizType={}, expireSeconds={}",
                 objectKey, credential.getUserId(), credential.getBizType(), expireSeconds);
    }

    @Override
    public UploadStsCredential getUploadCredential(String objectKey) {
        String redisKey = RedisKeyConstants.uploadStsKey(objectKey);
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value instanceof UploadStsCredential) {
            log.info("从Redis获取上传凭证: objectKey={}", objectKey);
            return (UploadStsCredential) value;
        }
        log.warn("上传凭证不存在或已过期: objectKey={}", objectKey);
        return null;
    }

    @Override
    public void deleteUploadCredential(String objectKey) {
        String redisKey = RedisKeyConstants.uploadStsKey(objectKey);
        Boolean deleted = redisTemplate.delete(redisKey);
        log.info("删除上传凭证: objectKey={}, deleted={}", objectKey, deleted);
    }

    @Override
    public UploadStsCredential validateUploadCredential(String objectKey, Long userId, String bizType) {
        // 1. 获取凭证
        UploadStsCredential credential = getUploadCredential(objectKey);
        if (credential == null) {
            throw new RuntimeException("上传凭证已过期或不存在");
        }

        // 2. 校验用户ID
        if (!credential.getUserId().equals(userId)) {
            log.error("上传凭证用户ID不匹配: objectKey={}, expectedUserId={}, actualUserId={}",
                     objectKey, userId, credential.getUserId());
            throw new RuntimeException("无权使用此上传凭证");
        }

        // 3. 校验业务类型
        if (!credential.getBizType().equals(bizType)) {
            log.error("上传凭证业务类型不匹配: objectKey={}, expectedBizType={}, actualBizType={}",
                     objectKey, bizType, credential.getBizType());
            throw new RuntimeException("业务类型不匹配");
        }

        log.info("上传凭证校验通过: objectKey={}, userId={}, bizType={}", objectKey, userId, bizType);
        return credential;
    }

    // ==================== 天气缓存相关 ====================

    @Override
    public void cacheTrailWeather(Long trailId, Object weatherData, long expireSeconds) {
        String redisKey = RedisKeyConstants.weatherTrailKey(trailId);
        redisTemplate.opsForValue().set(redisKey, weatherData, expireSeconds, TimeUnit.SECONDS);
        log.info("缓存路线天气: trailId={}, expireSeconds={}", trailId, expireSeconds);
    }

    @Override
    public Object getTrailWeatherCache(Long trailId) {
        String redisKey = RedisKeyConstants.weatherTrailKey(trailId);
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.info("天气缓存命中: trailId={}", trailId);
        } else {
            log.info("天气缓存未命中: trailId={}", trailId);
        }
        return value;
    }

    // ==================== 景观预测缓存相关 ====================

    @Override
    public void cacheLandscapePrediction(Long trailId, String date, Object predictionData, long expireSeconds) {
        String redisKey = RedisKeyConstants.landscapeTrailKey(trailId, date);
        redisTemplate.opsForValue().set(redisKey, predictionData, expireSeconds, TimeUnit.SECONDS);
        log.info("缓存景观预测: trailId={}, date={}, expireSeconds={}", trailId, date, expireSeconds);
    }

    @Override
    public Object getLandscapePredictionCache(Long trailId, String date) {
        String redisKey = RedisKeyConstants.landscapeTrailKey(trailId, date);
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.info("景观预测缓存命中: trailId={}, date={}", trailId, date);
        } else {
            log.info("景观预测缓存未命中: trailId={}, date={}", trailId, date);
        }
        return value;
    }

    // ==================== AI会话缓存相关 ====================

    @Override
    public void cacheAiConversationContext(Long userId, Object contextData, long expireSeconds) {
        String redisKey = RedisKeyConstants.aiConversationKey(userId);
        redisTemplate.opsForValue().set(redisKey, contextData, expireSeconds, TimeUnit.SECONDS);
        log.info("缓存AI会话上下文: userId={}, expireSeconds={}", userId, expireSeconds);
    }

    @Override
    public Object getAiConversationContextCache(Long userId) {
        String redisKey = RedisKeyConstants.aiConversationKey(userId);
        Object value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.info("AI会话缓存命中: userId={}", userId);
        } else {
            log.info("AI会话缓存未命中: userId={}", userId);
        }
        return value;
    }

    @Override
    public void invalidateAiConversationCache(Long userId) {
        String redisKey = RedisKeyConstants.aiConversationKey(userId);
        Boolean deleted = redisTemplate.delete(redisKey);
        log.info("清除AI会话缓存: userId={}, deleted={}", userId, deleted);
    }

    // ==================== 热门路线缓存相关 ====================

    @Override
    public void cacheHotTrails(Object trails, long expireSeconds) {
        redisTemplate.opsForValue().set(RedisKeyConstants.HOT_TRAILS, trails, expireSeconds, TimeUnit.SECONDS);
        log.info("缓存热门路线: expireSeconds={}", expireSeconds);
    }

    @Override
    public Object getHotTrailsCache() {
        Object value = redisTemplate.opsForValue().get(RedisKeyConstants.HOT_TRAILS);
        if (value != null) {
            log.info("热门路线缓存命中");
        } else {
            log.info("热门路线缓存未命中");
        }
        return value;
    }
}
