package com.sheng.hikingbackend.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("app_settings")
public class AppSetting {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("setting_key")
    private String settingKey;

    @TableField("setting_value")
    private String settingValue;

    private String description;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
