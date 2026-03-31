package com.sheng.hikingbackend.vo.config;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSystemOptionItemVo {
    private Long id;
    private String code;
    private String label;
    private String subLabel;
    private String description;
    private String icon;
    private Integer sort;
    private Boolean enabled;
    private Boolean builtin;
    private Map<String, Object> extra;
}
