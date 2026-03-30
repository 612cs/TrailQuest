package com.sheng.hikingbackend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.entity.AppSetting;
import com.sheng.hikingbackend.mapper.AppSettingMapper;
import com.sheng.hikingbackend.service.SiteSettingService;
import com.sheng.hikingbackend.vo.site.HomeHeroSettingVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSettingServiceImpl implements SiteSettingService {

    public static final String HOME_HERO_IMAGE_KEY = "home.hero.image_url";
    private static final String HOME_HERO_IMAGE_DESC = "首页首屏大图地址";

    private final AppSettingMapper appSettingMapper;

    @Override
    public HomeHeroSettingVo getHomeHeroSetting() {
        AppSetting setting = appSettingMapper.selectByKey(HOME_HERO_IMAGE_KEY);
        String imageUrl = setting == null ? null : normalize(setting.getSettingValue());
        return HomeHeroSettingVo.builder()
                .imageUrl(imageUrl)
                .usingDefault(!StringUtils.hasText(imageUrl))
                .updatedAt(setting == null ? null : setting.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateHomeHeroImage(Long adminUserId, String imageUrl) {
        String normalized = normalize(imageUrl);
        AppSetting existing = appSettingMapper.selectByKey(HOME_HERO_IMAGE_KEY);

        if (!StringUtils.hasText(normalized)) {
            if (existing != null) {
                appSettingMapper.deleteByKey(HOME_HERO_IMAGE_KEY);
            }
            return;
        }

        if (existing == null) {
            AppSetting setting = new AppSetting();
            setting.setSettingKey(HOME_HERO_IMAGE_KEY);
            setting.setSettingValue(normalized);
            setting.setDescription(HOME_HERO_IMAGE_DESC);
            setting.setUpdatedBy(adminUserId);
            appSettingMapper.insert(setting);
            return;
        }

        existing.setSettingValue(normalized);
        existing.setDescription(HOME_HERO_IMAGE_DESC);
        existing.setUpdatedBy(adminUserId);
        appSettingMapper.updateById(existing);
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
