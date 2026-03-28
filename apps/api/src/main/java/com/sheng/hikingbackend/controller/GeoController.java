package com.sheng.hikingbackend.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.dto.geo.ReverseGeoRequest;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @PostMapping("/reverse")
    public ApiResponse<ReverseGeoResponse> reverse(@Valid @RequestBody ReverseGeoRequest request) {
        return ApiResponse.success("地点识别成功", geoService.reverse(request.getLng(), request.getLat()));
    }
}
