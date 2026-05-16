package com.sheng.hikingbackend.constant;

/**
 * Redis Key常量类
 * 统一管理Redis中使用的Key格式
 */
public class RedisKeyConstants {

    // ==================== 上传凭证相关 ====================

    /**
     * 上传凭证前缀
     * 格式: upload:sts:{objectKey}
     * 用途: 存储OSS STS临时上传凭证信息
     * 过期时间: 3600秒（1小时）
     */
    public static final String UPLOAD_STS_PREFIX = "upload:sts:";

    /**
     * 生成上传凭证Key
     * @param objectKey OSS对象Key
     * @return Redis Key
     */
    public static String uploadStsKey(String objectKey) {
        return UPLOAD_STS_PREFIX + objectKey;
    }

    // ==================== 天气缓存相关 ====================

    /**
     * 天气缓存前缀
     * 格式: weather:trail:{trailId}
     * 用途: 缓存路线的天气查询结果
     * 过期时间: 1800秒（30分钟）
     */
    public static final String WEATHER_TRAIL_PREFIX = "weather:trail:";

    /**
     * 生成天气缓存Key
     * @param trailId 路线ID
     * @return Redis Key
     */
    public static String weatherTrailKey(Long trailId) {
        return WEATHER_TRAIL_PREFIX + trailId;
    }

    // ==================== 景观预测缓存相关 ====================

    /**
     * 景观预测缓存前缀
     * 格式: landscape:trail:{trailId}:{date}
     * 用途: 缓存路线的景观预测结果
     * 过期时间: 21600秒（6小时）
     */
    public static final String LANDSCAPE_TRAIL_PREFIX = "landscape:trail:";

    /**
     * 生成景观预测缓存Key
     * @param trailId 路线ID
     * @param date 预测日期（格式：yyyy-MM-dd）
     * @return Redis Key
     */
    public static String landscapeTrailKey(Long trailId, String date) {
        return LANDSCAPE_TRAIL_PREFIX + trailId + ":" + date;
    }

    // ==================== AI会话缓存相关 ====================

    /**
     * AI会话缓存前缀
     * 格式: ai:conversation:{userId}
     * 用途: 缓存用户的AI会话上下文
     * 过期时间: 900秒（15分钟）
     */
    public static final String AI_CONVERSATION_PREFIX = "ai:conversation:";

    /**
     * 生成AI会话缓存Key
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String aiConversationKey(Long userId) {
        return AI_CONVERSATION_PREFIX + userId;
    }

    // ==================== 热门路线缓存相关 ====================

    /**
     * 热门路线缓存Key
     * 格式: hot:trails
     * 用途: 缓存首页热门路线列表
     * 过期时间: 3600秒（1小时）
     */
    public static final String HOT_TRAILS = "hot:trails";

    // ==================== 用户会话相关 ====================

    /**
     * 用户在线状态前缀
     * 格式: user:online:{userId}
     * 用途: 标记用户在线状态
     * 过期时间: 1800秒（30分钟）
     */
    public static final String USER_ONLINE_PREFIX = "user:online:";

    /**
     * 生成用户在线状态Key
     * @param userId 用户ID
     * @return Redis Key
     */
    public static String userOnlineKey(Long userId) {
        return USER_ONLINE_PREFIX + userId;
    }
}
