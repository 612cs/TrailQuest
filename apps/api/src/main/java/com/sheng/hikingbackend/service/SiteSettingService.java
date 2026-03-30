package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.vo.site.HomeHeroSettingVo;

public interface SiteSettingService {

    HomeHeroSettingVo getHomeHeroSetting();

    void updateHomeHeroImage(Long adminUserId, String imageUrl);
}
