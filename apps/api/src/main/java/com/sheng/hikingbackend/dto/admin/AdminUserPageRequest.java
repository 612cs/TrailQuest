package com.sheng.hikingbackend.dto.admin;

import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserPageRequest extends PageRequest {

    private String keyword;
    private String role;

    public String getKeyword() {
        return normalize(keyword);
    }

    public String getRole() {
        return normalize(role);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
