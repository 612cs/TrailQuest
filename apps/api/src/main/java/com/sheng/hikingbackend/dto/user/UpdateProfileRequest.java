package com.sheng.hikingbackend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 50, message = "昵称长度需在 2 到 50 个字符之间")
    private String username;

    @Size(max = 300, message = "个人简介长度不能超过 300 个字符")
    private String bio;

    @Size(max = 100, message = "所在地长度不能超过 100 个字符")
    private String location;

    private Long avatarMediaId;
}
