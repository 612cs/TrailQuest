package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.dto.upload.CompleteUploadRequest;
import com.sheng.hikingbackend.dto.upload.CreateUploadStsRequest;
import com.sheng.hikingbackend.vo.upload.MediaFileVo;
import com.sheng.hikingbackend.vo.upload.UploadStsVo;

public interface UploadService {

    UploadStsVo createUploadSts(Long userId, CreateUploadStsRequest request);

    MediaFileVo completeUpload(Long userId, CompleteUploadRequest request);

    String resolveAvatarUrl(Long mediaFileId);
}
