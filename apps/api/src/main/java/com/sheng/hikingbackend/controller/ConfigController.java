package com.sheng.hikingbackend.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.service.OptionConfigService;
import com.sheng.hikingbackend.vo.config.PublicSystemOptionItemVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final OptionConfigService optionConfigService;

    @GetMapping("/options")
    public ApiResponse<Map<String, List<PublicSystemOptionItemVo>>> getOptions(
            @RequestParam(required = false) String groups) {
        List<String> groupCodes = groups == null || groups.isBlank()
                ? List.of()
                : Arrays.stream(groups.split(","))
                        .map(String::trim)
                        .filter(code -> !code.isBlank())
                        .toList();
        return ApiResponse.success(optionConfigService.getPublicOptions(groupCodes));
    }
}
