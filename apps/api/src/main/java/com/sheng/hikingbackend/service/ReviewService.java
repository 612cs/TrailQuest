package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.dto.review.CreateReviewRequest;
import com.sheng.hikingbackend.vo.review.CreateReviewResponse;
import com.sheng.hikingbackend.vo.review.DeleteReviewResponse;
import com.sheng.hikingbackend.vo.review.ReviewPageVo;

public interface ReviewService {

    ReviewPageVo listByTrailId(Long trailId, Long cursor, Integer limit);

    CreateReviewResponse createReview(Long userId, CreateReviewRequest request);

    DeleteReviewResponse deleteReview(Long userId, Long reviewId);

    DeleteReviewResponse deleteReviewAsAdmin(Long reviewId);
}
