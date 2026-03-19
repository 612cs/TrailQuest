package com.sheng.hikingbackend.vo.user;

import com.sheng.hikingbackend.entity.UserHikingProfile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HikingProfileVo {

    private String experienceLevel;
    private String trailStyle;
    private String packPreference;

    public static HikingProfileVo from(UserHikingProfile profile) {
        if (profile == null) {
            return null;
        }

        return HikingProfileVo.builder()
                .experienceLevel(profile.getExperienceLevel() == null ? null : profile.getExperienceLevel().getCode())
                .trailStyle(profile.getTrailStyle() == null ? null : profile.getTrailStyle().getCode())
                .packPreference(profile.getPackPreference() == null ? null : profile.getPackPreference().getCode())
                .build();
    }
}
