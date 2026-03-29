package com.sheng.hikingbackend.vo.ai;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiTrailCardVo {

    private Long id;
    private String image;
    private String name;
    private String location;
    private String description;
    private String difficulty;
    private String difficultyLabel;
    private String packType;
    private String durationType;
    private BigDecimal rating;
    private Integer reviewCount;
    private String reviews;
    private String distance;
    private String elevation;
    private String duration;
    private Integer likes;
    private Integer favorites;
    private Boolean likedByCurrentUser;
    private Boolean favoritedByCurrentUser;
    private String reason;
}
