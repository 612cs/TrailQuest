package com.sheng.hikingbackend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sheng.hikingbackend.common.enums.UserRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("users")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String avatar;

    @TableField("avatar_bg")
    private String avatarBg;

    @TableField("avatar_media_id")
    private Long avatarMediaId;

    private String bio;

    private String location;

    private String email;

    @TableField("password_hash")
    private String passwordHash;

    private UserRole role;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
