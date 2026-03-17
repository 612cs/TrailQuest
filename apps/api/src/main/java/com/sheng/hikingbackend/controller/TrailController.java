package com.sheng.hikingbackend.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/trails")
@RequiredArgsConstructor
public class TrailController {

    private final TrailService trailService;

    @GetMapping
    public ApiResponse<PageResponse<TrailDetailVo>> page(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid TrailPageRequest request) {
        return ApiResponse.success(trailService.pageTrails(request, userDetails == null ? null : userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TrailDetailVo> detail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(trailService.getTrailDetail(id, userDetails == null ? null : userDetails.getId()));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<TrailInteractionVo> likeTrail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success("点赞成功", trailService.likeTrail(id, userDetails.getId()));
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<TrailInteractionVo> unlikeTrail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success("已取消点赞", trailService.unlikeTrail(id, userDetails.getId()));
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<TrailInteractionVo> favoriteTrail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success("收藏成功", trailService.favoriteTrail(id, userDetails.getId()));
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<TrailInteractionVo> unfavoriteTrail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success("已取消收藏", trailService.unfavoriteTrail(id, userDetails.getId()));
    }
}
