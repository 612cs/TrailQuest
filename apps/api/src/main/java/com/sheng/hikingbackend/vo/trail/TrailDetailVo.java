package com.sheng.hikingbackend.vo.trail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sheng.hikingbackend.vo.common.UserSummaryVo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailDetailVo {

    private Long id;
    private String image;
    private String name;
    private String location;
    private String ip;
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
    private List<String> tags;
    private Integer favorites;
    private Integer likes;
    private Boolean likedByCurrentUser;
    private Boolean favoritedByCurrentUser;
    private Long authorId;
    private String publishTime;
    private LocalDateTime createdAt;
    private Boolean ownedByCurrentUser;
    private Boolean editableByCurrentUser;
    private Long coverMediaId;
    private List<TrailGalleryItemVo> gallery;
    private UserSummaryVo author;
    private TrailTrackVo track;
}
