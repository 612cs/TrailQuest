package com.sheng.hikingbackend.vo.review;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrailReviewStatsVo {

    private Long reviewCount;
    private BigDecimal averageRating;
}
