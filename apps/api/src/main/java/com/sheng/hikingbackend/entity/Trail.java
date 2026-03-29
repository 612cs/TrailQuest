package com.sheng.hikingbackend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("trails")
public class Trail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String image;
    private String name;
    private String location;
    private String ip;
    @TableField("start_lng")
    private BigDecimal startLng;
    @TableField("start_lat")
    private BigDecimal startLat;
    @TableField("geo_country")
    private String geoCountry;
    @TableField("geo_province")
    private String geoProvince;
    @TableField("geo_city")
    private String geoCity;
    @TableField("geo_district")
    private String geoDistrict;
    @TableField("geo_source")
    private String geoSource;
    private String difficulty;

    @TableField("difficulty_label")
    private String difficultyLabel;

    @TableField("pack_type")
    private String packType;

    @TableField("duration_type")
    private String durationType;

    private BigDecimal rating;

    @TableField("review_count")
    private Integer reviewCount;

    private String distance;
    private String elevation;
    private String duration;
    private String description;
    private Integer favorites;
    private Integer likes;

    @TableField("author_id")
    private Long authorId;

    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
