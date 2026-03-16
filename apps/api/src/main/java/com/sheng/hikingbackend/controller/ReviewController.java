package com.sheng.hikingbackend.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.review.CreateReviewRequest;
import com.sheng.hikingbackend.service.ReviewService;
import com.sheng.hikingbackend.vo.review.CreateReviewResponse;
import com.sheng.hikingbackend.vo.review.ReviewVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/trails/{trailId}/reviews")
    public ApiResponse<List<ReviewVo>> listByTrailId(@PathVariable Long trailId) {
        return ApiResponse.success(reviewService.listByTrailId(trailId));
    }

    @PostMapping("/reviews")
    public ApiResponse<CreateReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success("评论发布成功", reviewService.createReview(userDetails.getId(), request));
    }
}
