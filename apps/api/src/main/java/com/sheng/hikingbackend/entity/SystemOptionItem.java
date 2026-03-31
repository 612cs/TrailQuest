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
@TableName("system_option_items")
public class SystemOptionItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("group_id")
    private Long groupId;

    @TableField("item_code")
    private String itemCode;

    @TableField("item_label")
    private String itemLabel;

    @TableField("item_sub_label")
    private String itemSubLabel;

    private String description;

    @TableField("icon_name")
    private String iconName;

    @TableField("sort_order")
    private Integer sortOrder;

    private Boolean enabled;

    @TableField("is_builtin")
    private Boolean builtin;

    @TableField("extra_json")
    private String extraJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
