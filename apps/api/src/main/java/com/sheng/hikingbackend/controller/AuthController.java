package com.sheng.hikingbackend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.auth.LoginRequest;
import com.sheng.hikingbackend.dto.auth.RegisterRequest;
import com.sheng.hikingbackend.service.AuthService;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.auth.LoginResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("注册成功", authService.register(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(authService.getCurrentUser(userDetails.getId()));
    }
}
