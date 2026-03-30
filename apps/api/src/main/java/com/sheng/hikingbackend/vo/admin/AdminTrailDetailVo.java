package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailGalleryItemVo;
import com.sheng.hikingbackend.vo.trail.TrailTrackVo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTrailDetailVo {

    private Long id;
    private String image;
    private String name;
    private String location;
    private String geoCountry;
    private String geoProvince;
    private String geoCity;
    private String geoDistrict;
    private String geoSource;
    private String difficulty;
    private String difficultyLabel;
    private String packType;
    private String durationType;
    private String distance;
    private String elevation;
    private String duration;
    private String description;
    private List<String> tags;
    private Integer favorites;
    private Integer likes;
    private Integer reviewCount;
    private String reviewStatus;
    private String reviewRemark;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private UserSummaryVo author;
    private List<TrailGalleryItemVo> gallery;
    private TrailTrackVo track;
}
