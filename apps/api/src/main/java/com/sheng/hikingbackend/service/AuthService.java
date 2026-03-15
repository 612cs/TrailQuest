package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.dto.auth.LoginRequest;
import com.sheng.hikingbackend.dto.auth.RegisterRequest;
import com.sheng.hikingbackend.vo.auth.CurrentUserVo;
import com.sheng.hikingbackend.vo.auth.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse register(RegisterRequest request);

    CurrentUserVo getCurrentUser(Long userId);
}
