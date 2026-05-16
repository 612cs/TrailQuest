package com.sheng.hikingbackend.service.impl;

import com.sheng.hikingbackend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI推荐服务实现类（示例）
 * 展示如何使用Redis缓存AI会话上下文
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationServiceWithRedisExample {

    private final RedisCacheService redisCacheService;

    /**
     * 获取用户最近的会话上下文（带Redis缓存）
     *
     * 流程：
     * 1. 尝试从Redis获取缓存
     * 2. 缓存命中直接返回
     * 3. 缓存未命中，从数据库查询最近10条消息
     * 4. 写入Redis缓存，15分钟过期
     */
    public List<Object> getRecentContextWithCache(Long userId, Long conversationId) {

        // 1. 尝试从Redis获取缓存
        Object cachedContext = redisCacheService.getAiConversationContextCache(userId);

        if (cachedContext != null) {
            log.info("AI会话缓存命中，直接返回: userId={}", userId);
            return (List<Object>) cachedContext;
        }

        // 2. 缓存未命中，从数据库查询
        log.info("AI会话缓存未命中，查询数据库: userId={}, conversationId={}", userId, conversationId);

        // 3. 模拟从数据库查询最近10条消息
        List<Object> messages = mockQueryRecentMessages(conversationId);

        // 4. 写入Redis缓存，15分钟过期
        redisCacheService.cacheAiConversationContext(userId, messages, 900L);

        log.info("AI会话上下文查询成功并缓存: userId={}, messageCount={}", userId, messages.size());

        return messages;
    }

    /**
     * 新消息写入后，清除缓存
     *
     * 当用户发送新消息或AI回复新消息时，需要清除缓存
     * 确保下次获取的是最新的会话上下文
     */
    public void invalidateCacheAfterNewMessage(Long userId) {
        redisCacheService.invalidateAiConversationCache(userId);
        log.info("新消息写入，清除AI会话缓存: userId={}", userId);
    }

    /**
     * AI推荐流程（使用缓存的上下文）
     *
     * 流程：
     * 1. 获取用户会话上下文（从Redis或数据库）
     * 2. 调用大模型解析用户意图
     * 3. 查询候选路线
     * 4. 综合评分排序
     * 5. 生成推荐结果
     * 6. 保存新消息并清除缓存
     */
    public Object recommendTrailsWithContext(Long userId, Long conversationId, String userMessage) {

        // 1. 获取会话上下文（带缓存）
        List<Object> context = getRecentContextWithCache(userId, conversationId);
        log.info("获取会话上下文: userId={}, contextSize={}", userId, context.size());

        // 2. 模拟调用大模型解析意图
        Object parsedQuery = mockParseUserIntent(userMessage, context);
        log.info("解析用户意图: userId={}, message={}", userId, userMessage);

        // 3. 模拟查询候选路线
        List<Object> candidates = mockQueryCandidateTrails(parsedQuery);
        log.info("查询候选路线: userId={}, candidateCount={}", userId, candidates.size());

        // 4. 模拟综合评分排序
        List<Object> rankedTrails = mockRankTrails(candidates);
        log.info("路线排序完成: userId={}, topCount={}", userId, rankedTrails.size());

        // 5. 生成推荐结果
        Object recommendation = mockBuildRecommendation(rankedTrails);

        // 6. 保存新消息并清除缓存
        // aiMessageMapper.insert(userMessage);
        // aiMessageMapper.insert(assistantMessage);
        invalidateCacheAfterNewMessage(userId);

        return recommendation;
    }

    /**
     * 模拟从数据库查询最近消息
     */
    private List<Object> mockQueryRecentMessages(Long conversationId) {
        // 实际项目中这里会查询数据库
        // aiMessageMapper.selectList(...)
        List<Object> messages = new ArrayList<>();
        messages.add(new Object() {
            public String getRole() { return "user"; }
            public String getContent() { return "推荐一条适合新手的路线"; }
        });
        messages.add(new Object() {
            public String getRole() { return "assistant"; }
            public String getContent() { return "为您推荐武功山..."; }
        });
        return messages;
    }

    /**
     * 模拟解析用户意图
     */
    private Object mockParseUserIntent(String message, List<Object> context) {
        return new Object() {
            public String getLocation() { return "武功山"; }
            public String getDifficulty() { return "EASY"; }
            public String getPackType() { return "LIGHT"; }
        };
    }

    /**
     * 模拟查询候选路线
     */
    private List<Object> mockQueryCandidateTrails(Object query) {
        List<Object> trails = new ArrayList<>();
        trails.add(new Object() {
            public String getName() { return "武功山金顶"; }
            public double getScore() { return 85.5; }
        });
        trails.add(new Object() {
            public String getName() { return "武功山穿越"; }
            public double getScore() { return 78.3; }
        });
        return trails;
    }

    /**
     * 模拟路线排序
     */
    private List<Object> mockRankTrails(List<Object> candidates) {
        // 实际项目中这里会进行综合评分排序
        return candidates;
    }

    /**
     * 模拟构建推荐结果
     */
    private Object mockBuildRecommendation(List<Object> trails) {
        return new Object() {
            public List<Object> getTrails() { return trails; }
            public String getReason() { return "根据您的徒步经验和偏好推荐"; }
        };
    }
}
