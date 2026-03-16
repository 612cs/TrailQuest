package com.sheng.hikingbackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("review_images")
public class ReviewImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("review_id")
    private Long reviewId;

    private String image;
}
