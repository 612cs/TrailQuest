package com.sheng.hikingbackend.vo.review;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateReviewResponse {

    private ReviewVo review;
    private BigDecimal trailRating;
    private Integer trailReviewCount;
}
