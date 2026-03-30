package com.sheng.hikingbackend.dto.admin;

import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminTrailPageRequest extends PageRequest {

    private String reviewStatus;
    private String keyword;
    private String authorKeyword;

    public String getReviewStatus() {
        return normalize(reviewStatus);
    }

    public String getKeyword() {
        return normalize(keyword);
    }

    public String getAuthorKeyword() {
        return normalize(authorKeyword);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
