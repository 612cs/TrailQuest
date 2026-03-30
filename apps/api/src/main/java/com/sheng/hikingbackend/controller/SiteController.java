package com.sheng.hikingbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.service.SiteSettingService;
import com.sheng.hikingbackend.vo.site.HomeHeroSettingVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
public class SiteController {

    private final SiteSettingService siteSettingService;

    @GetMapping("/home-hero")
    public ApiResponse<HomeHeroSettingVo> getHomeHeroSetting() {
        return ApiResponse.success(siteSettingService.getHomeHeroSetting());
    }
}
