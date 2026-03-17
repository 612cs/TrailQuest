package com.sheng.hikingbackend.vo.review;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewPageVo {

    private List<ReviewVo> list;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long nextCursor;

    private boolean hasMore;
}
