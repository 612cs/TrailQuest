package com.sheng.hikingbackend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HikingProfileRequest {

    @NotBlank(message = "徒步经验不能为空")
    private String experienceLevel;

    @NotBlank(message = "常走类型不能为空")
    private String trailStyle;

    @NotBlank(message = "负重偏好不能为空")
    private String packPreference;
}
