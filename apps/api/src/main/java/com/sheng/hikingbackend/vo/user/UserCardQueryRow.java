package com.sheng.hikingbackend.vo.user;

import java.time.LocalDateTime;

import com.sheng.hikingbackend.common.enums.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCardQueryRow {

    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private UserRole role;
    private LocalDateTime createdAt;
    private Integer postCount;
    private Integer savedCount;
    private String location;
    private String bio;
}
