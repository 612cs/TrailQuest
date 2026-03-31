package com.sheng.hikingbackend.vo.config;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicSystemOptionItemVo {
    private String code;
    private String label;
    private String subLabel;
    private String description;
    private String icon;
    private Integer sort;
    private Boolean enabled;
    private Map<String, Object> extra;
}
