package com.sheng.hikingbackend.vo.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryVo {

    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
}
