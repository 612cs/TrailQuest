package com.sheng.hikingbackend.vo.trail;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrailGalleryItemVo {

    private Long mediaId;
    private String url;
}
