package com.sheng.hikingbackend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.common.PageRequest;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.user.UpdateHikingProfileRequest;
import com.sheng.hikingbackend.dto.user.UpdateProfileRequest;
import com.sheng.hikingbackend.service.UserService;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.user.UserCardVo;
import com.sheng.hikingbackend.vo.user.UserTrailListItemVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/card")
    public ApiResponse<UserCardVo> getUserCard(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserCard(id));
    }

    @GetMapping("/me/published-trails")
    public ApiResponse<PageResponse<UserTrailListItemVo>> getCurrentUserPublishedTrails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid PageRequest request) {
        return ApiResponse.success(userService.getCurrentUserPublishedTrails(userDetails.getId(), request));
    }

    @GetMapping("/me/favorite-trails")
    public ApiResponse<PageResponse<UserTrailListItemVo>> getCurrentUserFavoriteTrails(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid PageRequest request) {
        return ApiResponse.success(userService.getCurrentUserFavoriteTrails(userDetails.getId(), request));
    }

    @PutMapping("/me/profile")
    public ApiResponse<CurrentUserVo> updateCurrentUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("资料更新成功", userService.updateCurrentUserProfile(userDetails.getId(), request));
    }

    @PutMapping("/me/hiking-profile")
    public ApiResponse<CurrentUserVo> updateCurrentUserHikingProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateHikingProfileRequest request) {
        return ApiResponse.success("徒步画像更新成功", userService.updateCurrentUserHikingProfile(userDetails.getId(), request));
    }
}
