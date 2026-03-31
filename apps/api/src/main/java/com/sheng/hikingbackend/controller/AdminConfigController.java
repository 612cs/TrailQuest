package com.sheng.hikingbackend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.admin.AdminCreateSystemOptionItemRequest;
import com.sheng.hikingbackend.dto.admin.AdminUpdateSystemOptionItemRequest;
import com.sheng.hikingbackend.service.OptionConfigService;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionGroupVo;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionItemVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    private final OptionConfigService optionConfigService;

    @GetMapping("/groups")
    public ApiResponse<List<AdminSystemOptionGroupVo>> getGroups() {
        return ApiResponse.success(optionConfigService.getAdminGroups());
    }

    @GetMapping("/groups/{groupCode}/items")
    public ApiResponse<List<AdminSystemOptionItemVo>> getGroupItems(@PathVariable String groupCode) {
        return ApiResponse.success(optionConfigService.getAdminGroupItems(groupCode));
    }

    @PostMapping("/groups/{groupCode}/items")
    public ApiResponse<AdminSystemOptionItemVo> createGroupItem(
            @PathVariable String groupCode,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminCreateSystemOptionItemRequest request) {
        return ApiResponse.success("配置项创建成功",
                optionConfigService.createAdminGroupItem(groupCode, userDetails.getId(), request));
    }

    @PutMapping("/groups/{groupCode}/items/{itemId}")
    public ApiResponse<AdminSystemOptionItemVo> updateGroupItem(
            @PathVariable String groupCode,
            @PathVariable Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminUpdateSystemOptionItemRequest request) {
        return ApiResponse.success("配置项更新成功",
                optionConfigService.updateAdminGroupItem(groupCode, itemId, userDetails.getId(), request));
    }
}
