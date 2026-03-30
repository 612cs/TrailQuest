package com.sheng.hikingbackend.dto.admin;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminHomeHeroUpdateRequest {

    @Size(max = 1000, message = "图片地址长度不能超过 1000")
    private String imageUrl;
}
