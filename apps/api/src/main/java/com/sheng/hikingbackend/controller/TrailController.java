package com.sheng.hikingbackend.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/trails")
@RequiredArgsConstructor
public class TrailController {

    private final TrailService trailService;

    @GetMapping
    public ApiResponse<PageResponse<TrailDetailVo>> page(@Valid TrailPageRequest request) {
        return ApiResponse.success(trailService.pageTrails(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TrailDetailVo> detail(@PathVariable Long id) {
        return ApiResponse.success(trailService.getTrailDetail(id));
    }
}
