package com.sheng.hikingbackend.vo.auth;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.vo.user.HikingProfileVo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentUserVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long avatarMediaId;
    private String avatarMediaUrl;
    private String bio;
    private String location;
    private HikingProfileVo hikingProfile;
    private String email;
    private UserRole role;

    public static CurrentUserVo from(User user, String avatarMediaUrl, HikingProfileVo hikingProfile) {
        return CurrentUserVo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .avatarBg(user.getAvatarBg())
                .avatarMediaId(user.getAvatarMediaId())
                .avatarMediaUrl(avatarMediaUrl)
                .bio(user.getBio())
                .location(user.getLocation())
                .hikingProfile(hikingProfile)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
