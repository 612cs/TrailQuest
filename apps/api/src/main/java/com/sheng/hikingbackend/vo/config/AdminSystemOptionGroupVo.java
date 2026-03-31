package com.sheng.hikingbackend.vo.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminSystemOptionGroupVo {
    private String groupCode;
    private String groupName;
    private String description;
    private String status;
    private Integer itemCount;
    private Boolean allowCreate;
}
