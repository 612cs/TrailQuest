package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.dto.user.UpdateHikingProfileRequest;
import com.sheng.hikingbackend.dto.user.UpdateProfileRequest;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.user.UserCardVo;

public interface UserService {

    UserCardVo getUserCard(Long userId);

    CurrentUserVo updateCurrentUserProfile(Long userId, UpdateProfileRequest request);

    CurrentUserVo updateCurrentUserHikingProfile(Long userId, UpdateHikingProfileRequest request);
}
