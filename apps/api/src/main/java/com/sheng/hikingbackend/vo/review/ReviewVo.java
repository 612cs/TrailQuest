package com.sheng.hikingbackend.vo.review;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long trailId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    private Integer rating;
    private String time;
    private String text;
    private List<String> images;
    private List<ReviewVo> replies;
    private String replyTo;
    private ReviewAuthorVo author;
}
