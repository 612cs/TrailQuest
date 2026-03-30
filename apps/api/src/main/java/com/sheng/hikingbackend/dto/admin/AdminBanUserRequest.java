package com.sheng.hikingbackend.dto.admin;

import org.springframework.util.StringUtils;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminBanUserRequest {

    @NotBlank(message = "封禁原因不能为空")
    private String reason;

    public String getReason() {
        return StringUtils.hasText(reason) ? reason.trim() : null;
    }
}
