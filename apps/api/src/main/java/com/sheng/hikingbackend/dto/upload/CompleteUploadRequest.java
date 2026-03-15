package com.sheng.hikingbackend.dto.upload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteUploadRequest {

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "对象Key不能为空")
    private String objectKey;

    @NotBlank(message = "文件访问地址不能为空")
    private String url;

    private String originalName;
    private String extension;

    @NotBlank(message = "文件MIME类型不能为空")
    private String mimeType;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long size;

    private Integer width;
    private Integer height;
}
