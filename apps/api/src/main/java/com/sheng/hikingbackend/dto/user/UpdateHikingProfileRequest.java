package com.sheng.hikingbackend.dto.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHikingProfileRequest {

    @Valid
    @NotNull(message = "徒步画像不能为空")
    private HikingProfileRequest hikingProfile;
}
