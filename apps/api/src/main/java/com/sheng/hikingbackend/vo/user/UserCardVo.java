package com.sheng.hikingbackend.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.sheng.hikingbackend.common.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCardVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private UserRole role;
    private String joinDate;
    private Integer postCount;
    private Integer savedCount;
    private String location;
    private String bio;
}
