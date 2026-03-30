package com.sheng.hikingbackend.vo.site;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeHeroSettingVo {

    private String imageUrl;
    private boolean usingDefault;
    private LocalDateTime updatedAt;
}
