package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserQueryRow {

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
