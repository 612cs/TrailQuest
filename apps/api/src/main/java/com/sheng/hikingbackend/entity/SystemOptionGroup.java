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
@TableName("system_option_groups")
public class SystemOptionGroup {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("group_code")
    private String groupCode;

    @TableField("group_name")
    private String groupName;

    private String description;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
