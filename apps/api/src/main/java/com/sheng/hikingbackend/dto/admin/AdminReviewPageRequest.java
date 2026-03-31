package com.sheng.hikingbackend.dto.admin;

import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewPageRequest extends PageRequest {

    private String keyword;
    private String trailKeyword;
    private String authorKeyword;
    private String status;

    public String getKeyword() {
        return normalize(keyword);
    }

    public String getTrailKeyword() {
        return normalize(trailKeyword);
    }

    public String getAuthorKeyword() {
        return normalize(authorKeyword);
    }

    public String getStatus() {
        return normalize(status);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
