package com.sheng.hikingbackend.service.impl.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.entity.UserHikingProfile;
import com.sheng.hikingbackend.mapper.UserHikingProfileMapper;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.service.AiRouteRecommendationService;
import com.sheng.hikingbackend.service.DashScopeChatService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.impl.ai.model.AiIntent;
import com.sheng.hikingbackend.service.impl.ai.model.AiParsedQuery;
import com.sheng.hikingbackend.service.impl.ai.model.AiRecommendationResult;
import com.sheng.hikingbackend.service.impl.ai.model.DashScopeMessage;
import com.sheng.hikingbackend.vo.ai.AiFollowUpVo;
import com.sheng.hikingbackend.vo.ai.AiTrailCardVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.user.HikingProfileVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRouteRecommendationServiceImpl implements AiRouteRecommendationService {

    private static final Pattern DISTANCE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(km|公里)", Pattern.CASE_INSENSITIVE);
    private static final List<String> SCENE_TAGS = List.of("日出", "日落", "云海", "摄影", "湖泊", "露营", "森林", "古道", "溪流", "亲子", "新手");

    private final DashScopeChatService dashScopeChatService;
    private final TrailService trailService;
    private final UserHikingProfileMapper userHikingProfileMapper;
    private final ObjectMapper objectMapper;

    @Override
    public AiRecommendationResult planRecommendation(Long userId, String message) {
        UserHikingProfile profile = userHikingProfileMapper.selectByUserId(userId);
        String preferenceSummary = buildPreferenceSummary(profile);
        AiParsedQuery parsedQuery = parseQuery(message, preferenceSummary);
        if (parsedQuery.intent() != AiIntent.TRAIL_RECOMMENDATION) {
            return AiRecommendationResult.builder()
                    .intent(AiIntent.GENERAL_QA)
                    .parsedQuery(parsedQuery)
                    .trailCards(List.of())
                    .followUps(List.of(
                            AiFollowUpVo.builder().text("如果你想找路线，我可以按地点、难度或风景帮你推荐。") .build(),
                            AiFollowUpVo.builder().text("也可以直接告诉我你想去哪一带、走多长时间。") .build()))
                    .routeFactsSummary("")
                    .preferenceSummary(preferenceSummary)
                    .build();
        }

        TrailPageRequest request = new TrailPageRequest();
        request.setPageNum(1);
        request.setPageSize(12);
        request.setSort("hot");
        request.setDifficulty(parsedQuery.difficulty());
        request.setPackType(parsedQuery.packType());
        request.setDurationType(parsedQuery.durationType());
        request.setDistance(parsedQuery.distance());
        request.setKeyword(buildKeyword(parsedQuery, message));

        PageResponse<TrailDetailVo> result = trailService.pageTrails(request, userId);
        List<TrailDetailVo> ranked = result.getList().stream()
                .sorted(Comparator.comparingDouble((TrailDetailVo trail) -> scoreTrail(trail, parsedQuery, profile, message)).reversed())
                .limit(3)
                .toList();

        List<AiTrailCardVo> cards = ranked.stream()
                .map(trail -> toTrailCard(trail, parsedQuery, profile, message))
                .toList();

        return AiRecommendationResult.builder()
                .intent(AiIntent.TRAIL_RECOMMENDATION)
                .parsedQuery(parsedQuery)
                .trailCards(cards)
                .followUps(buildFollowUps(parsedQuery, cards.isEmpty()))
                .routeFactsSummary(buildRouteFacts(cards))
                .preferenceSummary(preferenceSummary)
                .build();
    }

    private AiParsedQuery parseQuery(String message, String preferenceSummary) {
        AiParsedQuery llmParsed = tryLlmParse(message, preferenceSummary);
        if (llmParsed != null) {
            return llmParsed;
        }
        return heuristicParse(message);
    }

    private AiParsedQuery tryLlmParse(String message, String preferenceSummary) {
        try {
            List<DashScopeMessage> messages = List.of(
                    DashScopeMessage.builder()
                            .role("system")
                            .content("你是 TrailQuest 的路线检索意图分析器。只返回 JSON，不要返回 markdown。JSON 结构固定为 {\"intent\":\"trail_recommendation|general_qa\",\"location\":string|null,\"difficulty\":\"easy|moderate|hard|null\",\"packType\":\"light|heavy|both|null\",\"durationType\":\"single_day|multi_day|null\",\"distance\":\"short|medium|long|null\",\"tags\":string[],\"keywords\":string[]}。")
                            .build(),
                    DashScopeMessage.builder()
                            .role("user")
                            .content("用户画像摘要：" + preferenceSummary + "\n用户消息：" + message)
                            .build());
            String rawJson = dashScopeChatService.completeJson(messages);
            JsonNode node = objectMapper.readTree(rawJson);
            return AiParsedQuery.builder()
                    .intent("trail_recommendation".equalsIgnoreCase(node.path("intent").asText()) ? AiIntent.TRAIL_RECOMMENDATION : AiIntent.GENERAL_QA)
                    .location(asNullable(node.path("location").asText(null)))
                    .difficulty(normalizeDifficulty(node.path("difficulty").asText(null)))
                    .packType(normalizePackType(node.path("packType").asText(null)))
                    .durationType(normalizeDurationType(node.path("durationType").asText(null)))
                    .distance(normalizeDistance(node.path("distance").asText(null)))
                    .tags(readStringArray(node.path("tags")))
                    .keywords(readStringArray(node.path("keywords")))
                    .build();
        } catch (Exception ex) {
            log.warn("AI query extraction fallback to heuristic", ex);
            return null;
        }
    }

    private AiParsedQuery heuristicParse(String message) {
        String normalized = message == null ? "" : message.trim();
        String difficulty = normalized.contains("新手") || normalized.contains("轻松") || normalized.contains("简单")
                ? "easy"
                : normalized.contains("进阶") || normalized.contains("适中")
                        ? "moderate"
                        : normalized.contains("挑战") || normalized.contains("虐") || normalized.contains("高难")
                                ? "hard"
                                : null;
        String packType = normalized.contains("重装") || normalized.contains("露营") ? "heavy"
                : normalized.contains("轻装") || normalized.contains("日常") ? "light"
                        : null;
        String durationType = normalized.contains("两天") || normalized.contains("多日") || normalized.contains("过夜") ? "multi_day"
                : normalized.contains("周末") || normalized.contains("单日") || normalized.contains("当天来回") ? "single_day"
                        : null;
        String distance = detectDistance(normalized);
        List<String> tags = SCENE_TAGS.stream().filter(normalized::contains).toList();
        boolean recommendationIntent = normalized.contains("推荐") || normalized.contains("路线") || normalized.contains("徒步") || normalized.contains("哪里");
        String location = detectLocation(normalized);
        List<String> keywords = new ArrayList<>(tags);
        if (StringUtils.hasText(location)) {
            keywords.add(location);
        }
        return AiParsedQuery.builder()
                .intent(recommendationIntent ? AiIntent.TRAIL_RECOMMENDATION : AiIntent.GENERAL_QA)
                .location(location)
                .difficulty(difficulty)
                .packType(packType)
                .durationType(durationType)
                .distance(distance)
                .tags(tags)
                .keywords(keywords)
                .build();
    }

    private double scoreTrail(TrailDetailVo trail, AiParsedQuery parsedQuery, UserHikingProfile profile, String message) {
        double score = 0;
        if (StringUtils.hasText(parsedQuery.location()) && trail.getLocation() != null && trail.getLocation().contains(parsedQuery.location())) {
            score += 35;
        }
        if (StringUtils.hasText(parsedQuery.difficulty()) && parsedQuery.difficulty().equals(trail.getDifficulty())) {
            score += 30;
        }
        if (StringUtils.hasText(parsedQuery.packType()) && parsedQuery.packType().equals(trail.getPackType())) {
            score += 20;
        }
        if (StringUtils.hasText(parsedQuery.durationType()) && parsedQuery.durationType().equals(trail.getDurationType())) {
            score += 20;
        }
        if (StringUtils.hasText(parsedQuery.distance()) && matchesDistanceBucket(trail.getDistance(), parsedQuery.distance())) {
            score += 18;
        }
        for (String tag : parsedQuery.tags()) {
            if (trail.getTags() != null && trail.getTags().contains(tag)) {
                score += 14;
            }
        }
        if (profile != null) {
            if (profile.getPackPreference() != null && profile.getPackPreference().getCode().equals(trail.getPackType())) {
                score += 8;
            }
            if (profile.getTrailStyle() != null && profile.getTrailStyle().getCode().equals("city_weekend") && "single_day".equals(trail.getDurationType())) {
                score += 6;
            }
            if (profile.getExperienceLevel() != null && profile.getExperienceLevel().getCode().equals("beginner") && "easy".equals(trail.getDifficulty())) {
                score += 10;
            }
        }
        if (trail.getName() != null && trail.getName().contains(extractCoreKeyword(message))) {
            score += 6;
        }
        score += trail.getRating().doubleValue() * 3;
        score += Math.min(trail.getFavorites(), 6000) / 400.0;
        score += Math.min(trail.getLikes(), 6000) / 500.0;
        return score;
    }

    private AiTrailCardVo toTrailCard(TrailDetailVo trail, AiParsedQuery parsedQuery, UserHikingProfile profile, String message) {
        return AiTrailCardVo.builder()
                .id(trail.getId())
                .image(trail.getImage())
                .name(trail.getName())
                .location(trail.getLocation())
                .description(trail.getDescription())
                .difficulty(trail.getDifficulty())
                .difficultyLabel(trail.getDifficultyLabel())
                .packType(trail.getPackType())
                .durationType(trail.getDurationType())
                .rating(trail.getRating())
                .reviewCount(trail.getReviewCount())
                .reviews(formatReviews(trail.getReviewCount()))
                .distance(trail.getDistance())
                .elevation(trail.getElevation())
                .duration(trail.getDuration())
                .likes(trail.getLikes())
                .favorites(trail.getFavorites())
                .likedByCurrentUser(Boolean.TRUE.equals(trail.getLikedByCurrentUser()))
                .favoritedByCurrentUser(Boolean.TRUE.equals(trail.getFavoritedByCurrentUser()))
                .reason(buildReason(trail, parsedQuery, profile, message))
                .build();
    }

    private String buildReason(TrailDetailVo trail, AiParsedQuery parsedQuery, UserHikingProfile profile, String message) {
        List<String> reasons = new ArrayList<>();
        if (StringUtils.hasText(parsedQuery.location()) && trail.getLocation().contains(parsedQuery.location())) {
            reasons.add("位置接近你提到的区域");
        }
        if (StringUtils.hasText(parsedQuery.difficulty()) && parsedQuery.difficulty().equals(trail.getDifficulty())) {
            reasons.add("强度与需求匹配");
        }
        if (StringUtils.hasText(parsedQuery.durationType()) && parsedQuery.durationType().equals(trail.getDurationType())) {
            reasons.add("行程安排更贴近你的时间预期");
        }
        if (trail.getTags() != null) {
            for (String tag : parsedQuery.tags()) {
                if (trail.getTags().contains(tag)) {
                    reasons.add("具备“" + tag + "”相关景观或体验");
                }
            }
        }
        if (reasons.isEmpty() && profile != null && profile.getExperienceLevel() != null && "beginner".equals(profile.getExperienceLevel().getCode())
                && "easy".equals(trail.getDifficulty())) {
            reasons.add("对新手更友好");
        }
        if (reasons.isEmpty()) {
            reasons.add("综合热度、评分和描述匹配度较高");
        }
        return String.join("，", reasons);
    }

    private List<AiFollowUpVo> buildFollowUps(AiParsedQuery parsedQuery, boolean emptyCards) {
        if (emptyCards) {
            return List.of(
                    AiFollowUpVo.builder().text("要不要把地点放宽一点，我可以再帮你找更接近的路线。") .build(),
                    AiFollowUpVo.builder().text("也可以告诉我你更在意轻松、风景还是日出拍照。") .build());
        }
        if ("single_day".equals(parsedQuery.durationType())) {
            return List.of(
                    AiFollowUpVo.builder().text("如果你想再轻松一点，我可以只保留新手友好的单日路线。") .build(),
                    AiFollowUpVo.builder().text("要不要我再按日出、云海或摄影感受帮你缩小范围？") .build());
        }
        return List.of(
                AiFollowUpVo.builder().text("如果你更在意风景体验，我可以再按标签帮你细分。") .build(),
                AiFollowUpVo.builder().text("也可以告诉我你大概能接受的距离和爬升。") .build());
    }

    private String buildRouteFacts(List<AiTrailCardVo> cards) {
        if (cards.isEmpty()) {
            return "当前内部路线库没有找到足够匹配的候选路线。";
        }
        StringBuilder builder = new StringBuilder("候选路线：\n");
        for (AiTrailCardVo card : cards) {
            builder.append("- ")
                    .append(card.getName())
                    .append(" | ")
                    .append(card.getLocation())
                    .append(" | 难度=")
                    .append(card.getDifficultyLabel())
                    .append(" | 距离=")
                    .append(card.getDistance())
                    .append(" | 时长=")
                    .append(card.getDuration())
                    .append(" | 推荐理由=")
                    .append(card.getReason())
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildPreferenceSummary(UserHikingProfile profile) {
        HikingProfileVo vo = HikingProfileVo.from(profile);
        if (vo == null) {
            return "用户暂未填写徒步画像，优先依据本轮消息进行推荐。";
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(vo.getExperienceLevel())) {
            parts.add("经验=" + vo.getExperienceLevel());
        }
        if (StringUtils.hasText(vo.getTrailStyle())) {
            parts.add("路线风格=" + vo.getTrailStyle());
        }
        if (StringUtils.hasText(vo.getPackPreference())) {
            parts.add("负重偏好=" + vo.getPackPreference());
        }
        return parts.isEmpty() ? "用户暂未填写徒步画像，优先依据本轮消息进行推荐。" : String.join("，", parts);
    }

    private String buildKeyword(AiParsedQuery parsedQuery, String message) {
        Set<String> keywords = new LinkedHashSet<>();
        if (StringUtils.hasText(parsedQuery.location())) {
            keywords.add(parsedQuery.location());
        }
        keywords.addAll(parsedQuery.tags());
        keywords.addAll(parsedQuery.keywords());
        if (keywords.isEmpty()) {
            String core = extractCoreKeyword(message);
            if (StringUtils.hasText(core)) {
                keywords.add(core);
            }
        }
        return keywords.isEmpty() ? null : String.join(" ", keywords);
    }

    private List<String> readStringArray(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = asNullable(item.asText(null));
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private String asNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeDifficulty(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "easy", "beginner", "simple" -> "easy";
            case "moderate", "medium" -> "moderate";
            case "hard", "expert" -> "hard";
            default -> null;
        };
    }

    private String normalizePackType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "light" -> "light";
            case "heavy" -> "heavy";
            case "both" -> "both";
            default -> null;
        };
    }

    private String normalizeDurationType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "single_day", "single day" -> "single_day";
            case "multi_day", "multi day" -> "multi_day";
            default -> null;
        };
    }

    private String normalizeDistance(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "short" -> "short";
            case "medium" -> "medium";
            case "long" -> "long";
            default -> null;
        };
    }

    private String detectDistance(String message) {
        Matcher matcher = DISTANCE_PATTERN.matcher(message);
        if (matcher.find()) {
            double km = Double.parseDouble(matcher.group(1));
            if (km < 5) {
                return "short";
            }
            if (km <= 10) {
                return "medium";
            }
            return "long";
        }
        if (message.contains("短线") || message.contains("短途")) {
            return "short";
        }
        if (message.contains("长线") || message.contains("长距离")) {
            return "long";
        }
        return null;
    }

    private String detectLocation(String message) {
        for (String marker : List.of("杭州", "临安", "湖州", "莫干山", "武功山", "大理", "牛背山", "神农架", "稻城", "江西", "浙江", "四川", "云南")) {
            if (message.contains(marker)) {
                return marker;
            }
        }
        return null;
    }

    private boolean matchesDistanceBucket(String distanceText, String bucket) {
        if (!StringUtils.hasText(distanceText)) {
            return false;
        }
        Matcher matcher = DISTANCE_PATTERN.matcher(distanceText.replace(" ", ""));
        if (!matcher.find()) {
            return false;
        }
        double km = Double.parseDouble(matcher.group(1));
        return switch (bucket) {
            case "short" -> km < 5;
            case "medium" -> km >= 5 && km <= 10;
            case "long" -> km > 10;
            default -> false;
        };
    }

    private String extractCoreKeyword(String message) {
        if (!StringUtils.hasText(message)) {
            return "";
        }
        String normalized = message.replaceAll("推荐|路线|徒步|帮我|想找|有没有|适合|最好|附近", " ").trim();
        return normalized.length() <= 8 ? normalized : normalized.substring(0, 8);
    }

    private String formatReviews(Integer reviewCount) {
        int safeCount = reviewCount == null ? 0 : reviewCount;
        if (safeCount >= 1000) {
            BigDecimal value = BigDecimal.valueOf(safeCount)
                    .divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            return "(" + value.toPlainString() + "k 条评论)";
        }
        return "(" + safeCount + " 条评论)";
    }
}
