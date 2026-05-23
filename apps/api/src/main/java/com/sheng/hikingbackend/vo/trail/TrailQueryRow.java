package com.sheng.hikingbackend.vo.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrailQueryRow {

    private Long id;
    private String image;
    private String name;
    private String location;
    private String ip;
    private String geoCountry;
    private String geoProvince;
    private String geoCity;
    private String geoDistrict;
    private String geoSource;
    private String difficulty;
    private String difficultyLabel;
    private String packType;
    private String durationType;
    private BigDecimal rating;
    private Integer reviewCount;
    private String distance;
    private String elevation;
    private String duration;
    private String description;
    private Integer favorites;
    private Integer likes;
    private Boolean likedByCurrentUser;
    private Boolean favoritedByCurrentUser;
    private Long authorId;
    private String sourceType;
    private String sourceSite;
    private String sourceUrl;
    private BigDecimal sourceConfidence;
    private String importBatchNo;
    private LocalDateTime createdAt;
    private String status;
    private String reviewStatus;
    private String aiReviewStatus;
    private String aiReviewReason;
    private String aiReviewRiskLevel;
    private String aiReviewCategoriesJson;
    private String aiReviewModel;
    private LocalDateTime aiReviewedAt;
    private String aiReviewTraceId;
    private String reviewRemark;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String tagsCsv;
    private String authorUsername;
    private String authorAvatar;
    private String authorAvatarBg;
}
