package com.sheng.hikingbackend.dto.upload;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUploadStsRequest {

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotBlank(message = "文件MIME类型不能为空")
    private String mimeType;
}
