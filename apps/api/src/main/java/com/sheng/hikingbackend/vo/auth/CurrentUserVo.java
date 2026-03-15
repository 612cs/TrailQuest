package com.sheng.hikingbackend.vo.auth;

import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentUserVo {

    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
    private Long avatarMediaId;
    private String avatarMediaUrl;
    private String email;
    private UserRole role;

    public static CurrentUserVo from(User user, String avatarMediaUrl) {
        return CurrentUserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .avatarBg(user.getAvatarBg())
                .avatarMediaId(user.getAvatarMediaId())
                .avatarMediaUrl(avatarMediaUrl)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
