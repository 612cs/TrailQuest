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
@TableName("trail_tracks")
public class TrailTrack {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("trail_id")
    private Long trailId;

    @TableField("media_file_id")
    private Long mediaFileId;

    @TableField("user_id")
    private Long userId;

    @TableField("source_format")
    private String sourceFormat;

    @TableField("original_file_name")
    private String originalFileName;

    @TableField("track_geojson")
    private String trackGeoJson;

    @TableField("track_points_count")
    private Integer trackPointsCount;

    @TableField("waypoint_count")
    private Integer waypointCount;

    @TableField("start_lng")
    private BigDecimal startLng;

    @TableField("start_lat")
    private BigDecimal startLat;

    @TableField("end_lng")
    private BigDecimal endLng;

    @TableField("end_lat")
    private BigDecimal endLat;

    @TableField("bbox_min_lng")
    private BigDecimal bboxMinLng;

    @TableField("bbox_min_lat")
    private BigDecimal bboxMinLat;

    @TableField("bbox_max_lng")
    private BigDecimal bboxMaxLng;

    @TableField("bbox_max_lat")
    private BigDecimal bboxMaxLat;

    @TableField("distance_meters")
    private BigDecimal distanceMeters;

    @TableField("elevation_min_meters")
    private BigDecimal elevationMinMeters;

    @TableField("elevation_peak_meters")
    private BigDecimal elevationPeakMeters;

    @TableField("elevation_gain_meters")
    private BigDecimal elevationGainMeters;

    @TableField("elevation_loss_meters")
    private BigDecimal elevationLossMeters;

    @TableField("duration_seconds")
    private Long durationSeconds;

    private String status;

    @TableField("parse_error_message")
    private String parseErrorMessage;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
