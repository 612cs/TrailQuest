package com.sheng.hikingbackend.dto.admin;

import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOperationLogPageRequest extends PageRequest {

    private String moduleCode;
    private String actionCode;
    private String operatorKeyword;
    private String targetType;
    private String targetId;
    private String dateFrom;
    private String dateTo;

    public String getModuleCode() {
        return normalize(moduleCode);
    }

    public String getActionCode() {
        return normalize(actionCode);
    }

    public String getOperatorKeyword() {
        return normalize(operatorKeyword);
    }

    public String getTargetType() {
        return normalize(targetType);
    }

    public String getTargetId() {
        return normalize(targetId);
    }

    public String getDateFrom() {
        return normalize(dateFrom);
    }

    public String getDateTo() {
        return normalize(dateTo);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
