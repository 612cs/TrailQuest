package com.sheng.hikingbackend.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.review.CreateReviewRequest;
import com.sheng.hikingbackend.entity.Review;
import com.sheng.hikingbackend.entity.ReviewImage;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.ReviewImageMapper;
import com.sheng.hikingbackend.mapper.ReviewMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.ReviewService;
import com.sheng.hikingbackend.vo.review.CreateReviewResponse;
import com.sheng.hikingbackend.vo.review.DeleteReviewResponse;
import com.sheng.hikingbackend.vo.review.ReviewPageVo;
import com.sheng.hikingbackend.vo.review.ReviewAuthorVo;
import com.sheng.hikingbackend.vo.review.ReviewQueryRow;
import com.sheng.hikingbackend.vo.review.ReviewVo;
import com.sheng.hikingbackend.vo.review.TrailReviewStatsVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 20;
    private static final String REVIEW_STATUS_ACTIVE = "active";
    private static final String REVIEW_STATUS_HIDDEN = "hidden";
    private static final String REVIEW_STATUS_DELETED = "deleted";

    private final ReviewMapper reviewMapper;
    private final ReviewImageMapper reviewImageMapper;
    private final TrailMapper trailMapper;
    private final UserMapper userMapper;

    @Override
    public ReviewPageVo listByTrailId(Long trailId, Long cursor, Integer limit) {
        ensureTrailExists(trailId);
        int pageSize = normalizeLimit(limit);
        List<ReviewQueryRow> topLevelRows = reviewMapper.selectTopLevelReviewRowsByTrailId(trailId, cursor, pageSize + 1);
        boolean hasMore = topLevelRows.size() > pageSize;
        if (hasMore) {
            topLevelRows = new ArrayList<>(topLevelRows.subList(0, pageSize));
        }

        if (topLevelRows.isEmpty()) {
            return ReviewPageVo.builder()
                    .list(List.of())
                    .nextCursor(null)
                    .hasMore(false)
                    .build();
        }

        List<Long> rootIds = topLevelRows.stream()
                .map(ReviewQueryRow::getId)
                .toList();
        List<ReviewQueryRow> rows = new ArrayList<>(topLevelRows);
        rows.addAll(reviewMapper.selectRepliesByRootIds(trailId, rootIds));
        Map<Long, List<String>> imagesByReviewId = loadImages(rows);
        Long nextCursor = hasMore ? topLevelRows.get(topLevelRows.size() - 1).getId() : null;
        return ReviewPageVo.builder()
                .list(buildReviewTree(rows, imagesByReviewId))
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    @Override
    @Transactional
    public CreateReviewResponse createReview(Long userId, CreateReviewRequest request) {
        Trail trail = ensureTrailExists(request.getTrailId());
        Review targetReview = validateParentReview(request);
        validatePayload(request, targetReview);
        Review storedParentReview = resolveStoredParentReview(targetReview);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        Review review = new Review();
        review.setTrailId(trail.getId());
        review.setUserId(userId);
        review.setParentId(storedParentReview == null ? null : storedParentReview.getId());
        review.setRating(targetReview == null ? request.getRating() : null);
        review.setText(request.getText().trim());
        review.setReplyTo(targetReview == null ? null : normalizeReplyTo(request.getReplyTo(), targetReview));
        review.setTimeLabel("刚刚");
        review.setStatus(REVIEW_STATUS_ACTIVE);
        review.setModerationReason(null);
        review.setModeratedBy(null);
        review.setModeratedAt(null);
        review.setCreatedAt(now);
        reviewMapper.insert(review);

        List<String> imageUrls = saveReviewImages(review.getId(), request.getImages());
        TrailReviewStatsVo stats = refreshTrailReviewStats(trail);

        ReviewVo reviewVo = ReviewVo.builder()
                .id(review.getId())
                .trailId(review.getTrailId())
                .userId(review.getUserId())
                .parentId(review.getParentId())
                .rating(review.getRating())
                .time(formatTimeLabel(now, review.getTimeLabel()))
                .text(review.getText())
                .images(imageUrls)
                .replies(List.of())
                .replyTo(review.getReplyTo())
                .author(ReviewAuthorVo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .avatar(user.getAvatar())
                        .avatarBg(user.getAvatarBg())
                        .avatarMediaUrl(null)
                        .build())
                .build();

        return CreateReviewResponse.builder()
                .review(reviewVo)
                .trailRating(scaleRating(stats.getAverageRating()))
                .trailReviewCount(stats.getReviewCount() == null ? 0 : stats.getReviewCount().intValue())
                .build();
    }

    @Override
    @Transactional
    public DeleteReviewResponse deleteReview(Long userId, Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw BusinessException.notFound("REVIEW_NOT_FOUND", "评论不存在");
        }
        if (!Objects.equals(review.getUserId(), userId)) {
            throw BusinessException.forbidden("REVIEW_DELETE_FORBIDDEN", "只能删除自己的评论");
        }
        return moderateReview(reviewId, userId, REVIEW_STATUS_DELETED, "用户自行删除");
    }

    @Override
    @Transactional
    public DeleteReviewResponse deleteReviewAsAdmin(Long reviewId) {
        return moderateReview(reviewId, null, REVIEW_STATUS_DELETED, "管理员删除");
    }

    @Override
    @Transactional
    public DeleteReviewResponse moderateReview(Long reviewId, Long operatorId, String status, String reason) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw BusinessException.notFound("REVIEW_NOT_FOUND", "评论不存在");
        }
        validateTargetStatus(status);
        if (Objects.equals(review.getStatus(), status)) {
            throw BusinessException.badRequest("REVIEW_STATUS_UNCHANGED", "评论已处于目标状态");
        }

        Trail trail = ensureTrailExists(review.getTrailId());
        review.setStatus(status);
        review.setModerationReason(reason == null || reason.isBlank() ? null : reason.trim());
        review.setModeratedBy(operatorId);
        review.setModeratedAt(LocalDateTime.now());
        reviewMapper.updateById(review);

        TrailReviewStatsVo stats = refreshTrailReviewStats(trail);
        return DeleteReviewResponse.builder()
                .deletedReviewId(reviewId)
                .trailRating(scaleRating(stats.getAverageRating()))
                .trailReviewCount(stats.getReviewCount() == null ? 0 : stats.getReviewCount().intValue())
                .build();
    }

    private Trail ensureTrailExists(Long trailId) {
        Trail trail = trailMapper.selectById(trailId);
        if (trail == null) {
            throw BusinessException.notFound("TRAIL_NOT_FOUND", "路线不存在");
        }
        return trail;
    }

    private Review validateParentReview(CreateReviewRequest request) {
        if (request.getParentId() == null) {
            return null;
        }

        Review parentReview = reviewMapper.selectById(request.getParentId());
        if (parentReview == null
                || !REVIEW_STATUS_ACTIVE.equals(parentReview.getStatus())
                || !belongsToTrail(parentReview, request.getTrailId())) {
            throw BusinessException.badRequest("PARENT_REVIEW_NOT_FOUND", "回复目标不存在");
        }
        return parentReview;
    }

    private boolean belongsToTrail(Review review, Long trailId) {
        Review current = review;
        while (current != null) {
            if (Objects.equals(current.getTrailId(), trailId)) {
                return true;
            }
            if (current.getParentId() == null) {
                return false;
            }
            current = reviewMapper.selectById(current.getParentId());
        }
        return false;
    }

    private void validatePayload(CreateReviewRequest request, Review targetReview) {
        boolean isTopLevel = targetReview == null;
        if (isTopLevel && request.getRating() == null) {
            throw BusinessException.badRequest("REVIEW_RATING_REQUIRED", "顶级评论需要评分");
        }
        if (!isTopLevel && request.getRating() != null) {
            throw BusinessException.badRequest("REPLY_RATING_NOT_ALLOWED", "回复评论不能评分");
        }
    }

    private Review resolveStoredParentReview(Review targetReview) {
        if (targetReview == null) {
            return null;
        }

        List<Review> lineage = loadReviewLineage(targetReview);
        if (lineage.size() <= 2) {
            return targetReview;
        }
        return lineage.get(1);
    }

    private int getReviewDepth(Review review) {
        return loadReviewLineage(review).size();
    }

    private String normalizeReplyTo(String replyTo, Review parentReview) {
        if (replyTo != null && !replyTo.isBlank()) {
            return replyTo.trim();
        }

        User parentUser = userMapper.selectById(parentReview.getUserId());
        return parentUser == null ? null : parentUser.getUsername();
    }

    private List<Review> loadReviewLineage(Review review) {
        List<Review> lineage = new ArrayList<>();
        Review current = review;
        while (current != null) {
            lineage.add(0, current);
            if (current.getParentId() == null) {
                break;
            }
            current = reviewMapper.selectById(current.getParentId());
        }
        return lineage;
    }

    private void validateTargetStatus(String status) {
        if (!REVIEW_STATUS_HIDDEN.equals(status) && !REVIEW_STATUS_DELETED.equals(status) && !REVIEW_STATUS_ACTIVE.equals(status)) {
            throw BusinessException.badRequest("REVIEW_STATUS_INVALID", "评论状态非法");
        }
    }

    private List<String> saveReviewImages(Long reviewId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<ReviewImage> reviewImages = images.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .map(url -> {
                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setReviewId(reviewId);
                    reviewImage.setImage(url);
                    return reviewImage;
                })
                .toList();

        if (reviewImages.isEmpty()) {
            return List.of();
        }

        reviewImageMapper.insertBatch(reviewImages);
        return reviewImages.stream()
                .map(ReviewImage::getImage)
                .toList();
    }

    private TrailReviewStatsVo refreshTrailReviewStats(Trail trail) {
        TrailReviewStatsVo stats = reviewMapper.selectTrailReviewStats(trail.getId());
        int reviewCount = stats.getReviewCount() == null ? 0 : stats.getReviewCount().intValue();
        BigDecimal averageRating = scaleRating(stats.getAverageRating());

        trail.setReviewCount(reviewCount);
        trail.setRating(averageRating);
        trailMapper.updateById(trail);
        return stats;
    }

    private BigDecimal scaleRating(BigDecimal rating) {
        if (rating == null) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return rating.setScale(1, RoundingMode.HALF_UP);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, MAX_PAGE_SIZE);
    }

    private Map<Long, List<String>> loadImages(List<ReviewQueryRow> rows) {
        if (rows.isEmpty()) {
            return Map.of();
        }
        List<Long> reviewIds = rows.stream()
                .map(ReviewQueryRow::getId)
                .toList();
        List<ReviewImage> reviewImages = reviewImageMapper.selectByReviewIds(reviewIds);
        Map<Long, List<String>> imagesByReviewId = new HashMap<>();
        for (ReviewImage reviewImage : reviewImages) {
            imagesByReviewId.computeIfAbsent(reviewImage.getReviewId(), _key -> new ArrayList<>())
                    .add(reviewImage.getImage());
        }
        return imagesByReviewId;
    }

    private List<ReviewVo> buildReviewTree(List<ReviewQueryRow> rows, Map<Long, List<String>> imagesByReviewId) {
        Map<Long, ReviewNode> nodes = new LinkedHashMap<>();
        for (ReviewQueryRow row : rows) {
            nodes.put(row.getId(), new ReviewNode(row, imagesByReviewId.getOrDefault(row.getId(), List.of())));
        }

        List<ReviewNode> roots = new ArrayList<>();
        for (ReviewNode node : nodes.values()) {
            Long parentId = node.row().getParentId();
            if (parentId == null) {
                roots.add(node);
                continue;
            }

            ReviewNode parentNode = nodes.get(parentId);
            if (parentNode == null) {
                roots.add(node);
                continue;
            }
            parentNode.children().add(node);
        }

        roots.sort(Comparator.comparing((ReviewNode node) -> node.row().getCreatedAt()).reversed()
                .thenComparing(node -> node.row().getId(), Comparator.reverseOrder()));
        roots.forEach(this::sortChildrenRecursively);

        return roots.stream()
                .map(this::toReviewVo)
                .toList();
    }

    private void sortChildrenRecursively(ReviewNode node) {
        node.children().sort(Comparator.comparing((ReviewNode child) -> child.row().getCreatedAt())
                .thenComparing(child -> child.row().getId()));
        node.children().forEach(this::sortChildrenRecursively);
    }

    private ReviewVo toReviewVo(ReviewNode node) {
        ReviewQueryRow row = node.row();
        return ReviewVo.builder()
                .id(row.getId())
                .trailId(row.getTrailId())
                .userId(row.getUserId())
                .parentId(row.getParentId())
                .rating(row.getRating())
                .time(formatTimeLabel(row.getCreatedAt(), row.getTimeLabel()))
                .text(row.getText())
                .images(node.images())
                .replies(node.children().stream().map(this::toReviewVo).toList())
                .replyTo(row.getReplyTo())
                .author(ReviewAuthorVo.builder()
                        .id(row.getUserId())
                        .username(row.getUsername())
                        .avatar(row.getAvatar())
                        .avatarBg(row.getAvatarBg())
                        .avatarMediaUrl(row.getAvatarMediaUrl())
                        .build())
                .build();
    }

    private String formatTimeLabel(LocalDateTime createdAt, String fallback) {
        if (createdAt == null) {
            return fallback == null ? "" : fallback;
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long minutes = Math.max(duration.toMinutes(), 0);
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }

        long days = duration.toDays();
        if (days < 7) {
            return days + " 天前";
        }
        if (days < 30) {
            long weeks = Math.max(days / 7, 1);
            return weeks + " 周前";
        }

        long months = days / 30;
        if (months < 12) {
            return months + " 个月前";
        }

        long years = Math.max(months / 12, 1);
        return years + " 年前";
    }

    private record ReviewNode(ReviewQueryRow row, List<String> images, List<ReviewNode> children) {
        private ReviewNode(ReviewQueryRow row, List<String> images) {
            this(row, images, new ArrayList<>());
        }
    }
}
