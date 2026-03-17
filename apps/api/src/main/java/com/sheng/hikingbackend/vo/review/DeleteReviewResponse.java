package com.sheng.hikingbackend.vo.review;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteReviewResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long deletedReviewId;

    private BigDecimal trailRating;
    private Integer trailReviewCount;
}
