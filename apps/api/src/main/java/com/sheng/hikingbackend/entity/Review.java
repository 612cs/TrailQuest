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
@TableName("reviews")
public class Review {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("trail_id")
    private Long trailId;

    @TableField("user_id")
    private Long userId;

    @TableField("parent_id")
    private Long parentId;

    private Integer rating;

    @TableField("time_label")
    private String timeLabel;

    private String text;

    @TableField("reply_to")
    private String replyTo;

    private String status;

    @TableField("moderation_reason")
    private String moderationReason;

    @TableField("moderated_by")
    private Long moderatedBy;

    @TableField("moderated_at")
    private LocalDateTime moderatedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
