package com.sheng.hikingbackend.service.impl;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.enums.HikingExperienceLevel;
import com.sheng.hikingbackend.common.enums.HikingPackPreference;
import com.sheng.hikingbackend.common.enums.HikingTrailStyle;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.config.JwtTokenProvider;
import com.sheng.hikingbackend.dto.auth.LoginRequest;
import com.sheng.hikingbackend.dto.auth.RegisterRequest;
import com.sheng.hikingbackend.dto.user.HikingProfileRequest;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.entity.UserHikingProfile;
import com.sheng.hikingbackend.mapper.UserHikingProfileMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AuthService;
import com.sheng.hikingbackend.service.UploadService;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.auth.LoginResponse;
import com.sheng.hikingbackend.vo.user.HikingProfileVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final List<String> AVATAR_BG_PALETTE = List.of(
            "#8b5cf6",
            "#0891b2",
            "#2563eb",
            "#059669",
            "#ea580c",
            "#dc2626");

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserHikingProfileMapper userHikingProfileMapper;
    private final UploadService uploadService;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userMapper.selectById(userDetails.getId());
            return LoginResponse.builder()
                    .accessToken(jwtTokenProvider.generateToken(userDetails))
                    .tokenType("Bearer")
                    .expiresInSeconds(jwtTokenProvider.getExpiresInSeconds())
                    .user(buildCurrentUser(user))
                    .build();
        } catch (BadCredentialsException ex) {
            throw BusinessException.unauthorized("INVALID_CREDENTIALS", "邮箱或密码不正确");
        }
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        Long emailCount = userMapper.countByEmail(request.getEmail());
        if (emailCount != null && emailCount > 0) {
            throw BusinessException.badRequest("EMAIL_ALREADY_EXISTS", "该邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setAvatar(buildAvatar(user.getUsername()));
        user.setAvatarBg(pickAvatarBg(request.getEmail()));
        user.setBio(null);
        user.setLocation(normalizeNullable(request.getLocation()));
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        userMapper.insert(user);
        saveHikingProfile(user.getId(), request.getHikingProfile());

        CustomUserDetails userDetails = CustomUserDetails.from(user);
        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(userDetails))
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getExpiresInSeconds())
                .user(buildCurrentUser(user))
                .build();
    }

    @Override
    public CurrentUserVo getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }
        return buildCurrentUser(user);
    }

    private String buildAvatar(String username) {
        String normalized = username == null ? "" : username.replaceAll("\\s+", "");
        if (normalized.isBlank()) {
            return "U";
        }
        int endIndex = Math.min(normalized.length(), 2);
        return normalized.substring(0, endIndex).toUpperCase();
    }

    private String pickAvatarBg(String email) {
        int index = Math.abs(email.hashCode()) % AVATAR_BG_PALETTE.size();
        return AVATAR_BG_PALETTE.get(index);
    }

    private CurrentUserVo buildCurrentUser(User user) {
        return CurrentUserVo.from(
                user,
                uploadService.resolveAvatarUrl(user.getAvatarMediaId()),
                HikingProfileVo.from(userHikingProfileMapper.selectByUserId(user.getId())));
    }

    private void saveHikingProfile(Long userId, HikingProfileRequest request) {
        if (request == null) {
            return;
        }

        UserHikingProfile profile = new UserHikingProfile();
        profile.setUserId(userId);
        profile.setExperienceLevel(requireExperienceLevel(request.getExperienceLevel()));
        profile.setTrailStyle(requireTrailStyle(request.getTrailStyle()));
        profile.setPackPreference(requirePackPreference(request.getPackPreference()));
        userHikingProfileMapper.insert(profile);
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

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
