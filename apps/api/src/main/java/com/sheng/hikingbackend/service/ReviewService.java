package com.sheng.hikingbackend.service;

import java.util.List;

import com.sheng.hikingbackend.dto.review.CreateReviewRequest;
import com.sheng.hikingbackend.vo.review.CreateReviewResponse;
import com.sheng.hikingbackend.vo.review.ReviewVo;

public interface ReviewService {

    List<ReviewVo> listByTrailId(Long trailId);

    CreateReviewResponse createReview(Long userId, CreateReviewRequest request);
}
