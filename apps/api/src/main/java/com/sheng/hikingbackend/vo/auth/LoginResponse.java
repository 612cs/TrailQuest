package com.sheng.hikingbackend.vo.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private long expiresInSeconds;
    private CurrentUserVo user;
}
