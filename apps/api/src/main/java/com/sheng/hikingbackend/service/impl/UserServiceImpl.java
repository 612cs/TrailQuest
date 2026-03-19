package com.sheng.hikingbackend.service.impl;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sheng.hikingbackend.common.enums.HikingExperienceLevel;
import com.sheng.hikingbackend.common.enums.HikingPackPreference;
import com.sheng.hikingbackend.common.enums.HikingTrailStyle;
import com.sheng.hikingbackend.common.enums.MediaBizType;
import com.sheng.hikingbackend.common.enums.MediaFileStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.user.HikingProfileRequest;
import com.sheng.hikingbackend.dto.user.UpdateHikingProfileRequest;
import com.sheng.hikingbackend.dto.user.UpdateProfileRequest;
import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.entity.UserHikingProfile;
import com.sheng.hikingbackend.mapper.MediaFileMapper;
import com.sheng.hikingbackend.mapper.UserHikingProfileMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.UserService;
import com.sheng.hikingbackend.service.UploadService;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.user.HikingProfileVo;
import com.sheng.hikingbackend.vo.user.UserCardQueryRow;
import com.sheng.hikingbackend.vo.user.UserCardVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter JOIN_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月加入");
    private static final String DEFAULT_LOCATION = "未知地区";
    private static final String DEFAULT_BIO = "此用户没有填写个人简介";

    private final UserMapper userMapper;
    private final MediaFileMapper mediaFileMapper;
    private final UserHikingProfileMapper userHikingProfileMapper;
    private final UploadService uploadService;

    @Override
    public UserCardVo getUserCard(Long userId) {
        UserCardQueryRow row = userMapper.selectUserCardById(userId);
        if (row == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        return UserCardVo.builder()
                .id(row.getId())
                .username(row.getUsername())
                .avatar(row.getAvatar())
                .avatarBg(row.getAvatarBg())
                .avatarMediaUrl(row.getAvatarMediaUrl())
                .role(row.getRole())
                .joinDate(row.getCreatedAt() == null ? "" : row.getCreatedAt().format(JOIN_DATE_FORMATTER))
                .postCount(row.getPostCount() == null ? 0 : row.getPostCount())
                .savedCount(row.getSavedCount() == null ? 0 : row.getSavedCount())
                .location(defaultIfBlank(row.getLocation(), DEFAULT_LOCATION))
                .bio(defaultIfBlank(row.getBio(), DEFAULT_BIO))
                .build();
    }

    @Override
    public CurrentUserVo updateCurrentUserProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        user.setUsername(request.getUsername().trim());
        user.setAvatar(buildAvatar(user.getUsername()));
        user.setBio(normalizeNullable(request.getBio()));
        user.setLocation(normalizeNullable(request.getLocation()));

        if (request.getAvatarMediaId() != null) {
            MediaFile avatarMedia = mediaFileMapper.selectOne(new LambdaQueryWrapper<MediaFile>()
                    .eq(MediaFile::getId, request.getAvatarMediaId())
                    .eq(MediaFile::getUserId, userId)
                    .eq(MediaFile::getBizType, MediaBizType.AVATAR.getCode())
                    .eq(MediaFile::getStatus, MediaFileStatus.ACTIVE.getCode())
                    .last("LIMIT 1"));
            if (avatarMedia == null) {
                throw BusinessException.badRequest("INVALID_AVATAR_MEDIA", "头像文件不存在或无权使用");
            }
            user.setAvatarMediaId(avatarMedia.getId());
        }

        userMapper.updateById(user);
        return buildCurrentUser(user);
    }

    @Override
    public CurrentUserVo updateCurrentUserHikingProfile(Long userId, UpdateHikingProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        UserHikingProfile existingProfile = userHikingProfileMapper.selectByUserId(userId);
        HikingProfileRequest payload = request.getHikingProfile();

        if (existingProfile == null) {
            existingProfile = new UserHikingProfile();
            existingProfile.setUserId(userId);
            applyHikingProfile(existingProfile, payload);
            userHikingProfileMapper.insert(existingProfile);
        } else {
            applyHikingProfile(existingProfile, payload);
            userHikingProfileMapper.updateById(existingProfile);
        }

        return buildCurrentUser(user);
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String buildAvatar(String username) {
        String normalized = username == null ? "" : username.replaceAll("\\s+", "");
        if (normalized.isBlank()) {
            return "U";
        }
        int endIndex = Math.min(normalized.length(), 2);
        return normalized.substring(0, endIndex).toUpperCase();
    }

    private CurrentUserVo buildCurrentUser(User user) {
        return CurrentUserVo.from(
                user,
                uploadService.resolveAvatarUrl(user.getAvatarMediaId()),
                HikingProfileVo.from(userHikingProfileMapper.selectByUserId(user.getId())));
    }

    private void applyHikingProfile(UserHikingProfile profile, HikingProfileRequest request) {
        profile.setExperienceLevel(requireExperienceLevel(request.getExperienceLevel()));
        profile.setTrailStyle(requireTrailStyle(request.getTrailStyle()));
        profile.setPackPreference(requirePackPreference(request.getPackPreference()));
    }

    private HikingExperienceLevel requireExperienceLevel(String code) {
        HikingExperienceLevel value = HikingExperienceLevel.fromCode(code);
        if (value == null) {
            throw BusinessException.badRequest("INVALID_EXPERIENCE_LEVEL", "徒步经验选项不合法");
        }
        return value;
    }

    private HikingTrailStyle requireTrailStyle(String code) {
        HikingTrailStyle value = HikingTrailStyle.fromCode(code);
        if (value == null) {
            throw BusinessException.badRequest("INVALID_TRAIL_STYLE", "常走类型选项不合法");
        }
        return value;
    }

    private HikingPackPreference requirePackPreference(String code) {
        HikingPackPreference value = HikingPackPreference.fromCode(code);
        if (value == null) {
            throw BusinessException.badRequest("INVALID_PACK_PREFERENCE", "负重偏好选项不合法");
        }
        return value;
    }
}
