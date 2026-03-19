package com.sheng.hikingbackend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sheng.hikingbackend.common.enums.HikingExperienceLevel;
import com.sheng.hikingbackend.common.enums.HikingPackPreference;
import com.sheng.hikingbackend.common.enums.HikingTrailStyle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("user_hiking_profiles")
public class UserHikingProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("experience_level")
    private HikingExperienceLevel experienceLevel;

    @TableField("trail_style")
    private HikingTrailStyle trailStyle;

    @TableField("pack_preference")
    private HikingPackPreference packPreference;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
