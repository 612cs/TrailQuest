package com.sheng.hikingbackend.vo.review;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewAuthorVo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String avatar;
    private String avatarBg;
    private String avatarMediaUrl;
}
