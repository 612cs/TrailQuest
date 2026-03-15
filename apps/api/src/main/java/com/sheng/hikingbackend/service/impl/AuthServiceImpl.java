package com.sheng.hikingbackend.service.impl;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.config.JwtTokenProvider;
import com.sheng.hikingbackend.dto.auth.LoginRequest;
import com.sheng.hikingbackend.dto.auth.RegisterRequest;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AuthService;
import com.sheng.hikingbackend.service.UploadService;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.auth.LoginResponse;

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
                    .user(CurrentUserVo.from(user, uploadService.resolveAvatarUrl(user.getAvatarMediaId())))
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
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        userMapper.insert(user);

        CustomUserDetails userDetails = CustomUserDetails.from(user);
        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(userDetails))
                .tokenType("Bearer")
                .expiresInSeconds(jwtTokenProvider.getExpiresInSeconds())
                .user(CurrentUserVo.from(user, uploadService.resolveAvatarUrl(user.getAvatarMediaId())))
                .build();
    }

    @Override
    public CurrentUserVo getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }
        return CurrentUserVo.from(user, uploadService.resolveAvatarUrl(user.getAvatarMediaId()));
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
}
