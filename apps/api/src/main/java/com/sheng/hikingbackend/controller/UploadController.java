package com.sheng.hikingbackend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.dto.upload.CompleteUploadRequest;
import com.sheng.hikingbackend.dto.upload.CreateUploadStsRequest;
import com.sheng.hikingbackend.service.UploadService;
import com.sheng.hikingbackend.vo.upload.MediaFileVo;
import com.sheng.hikingbackend.vo.upload.UploadStsVo;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/sts")
    public ApiResponse<UploadStsVo> createUploadSts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateUploadStsRequest request) {
        return ApiResponse.success("上传凭证获取成功", uploadService.createUploadSts(userDetails.getId(), request));
    }

    @PostMapping("/complete")
    public ApiResponse<MediaFileVo> completeUpload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CompleteUploadRequest request) {
        return ApiResponse.success("文件记录保存成功", uploadService.completeUpload(userDetails.getId(), request));
    }
}
