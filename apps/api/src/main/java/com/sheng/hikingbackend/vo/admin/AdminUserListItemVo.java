package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserListItemVo {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String location;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
    private String status;
    private LocalDateTime bannedAt;
    private Integer publishedTrailCount;
    private LocalDateTime createdAt;
}
