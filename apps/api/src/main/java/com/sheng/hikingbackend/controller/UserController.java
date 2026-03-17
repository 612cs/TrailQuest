package com.sheng.hikingbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.service.UserService;
import com.sheng.hikingbackend.vo.user.UserCardVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}/card")
    public ApiResponse<UserCardVo> getUserCard(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserCard(id));
    }
}
