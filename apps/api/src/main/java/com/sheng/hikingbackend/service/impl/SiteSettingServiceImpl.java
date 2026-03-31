package com.sheng.hikingbackend.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.entity.AppSetting;
import com.sheng.hikingbackend.mapper.AppSettingMapper;
import com.sheng.hikingbackend.service.AdminOperationLogService;
import com.sheng.hikingbackend.service.SiteSettingService;
import com.sheng.hikingbackend.vo.site.HomeHeroSettingVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteSettingServiceImpl implements SiteSettingService {

    public static final String HOME_HERO_IMAGE_KEY = "home.hero.image_url";
    private static final String HOME_HERO_IMAGE_DESC = "首页首屏大图地址";

    private final AppSettingMapper appSettingMapper;
    private final AdminOperationLogService adminOperationLogService;

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
        String previousImageUrl = existing == null ? null : normalize(existing.getSettingValue());

        if (!StringUtils.hasText(normalized)) {
            if (existing != null) {
                appSettingMapper.deleteByKey(HOME_HERO_IMAGE_KEY);
                adminOperationLogService.record(
                        adminUserId,
                        "setting_management",
                        "home_hero_update",
                        "setting",
                        existing.getId(),
                        HOME_HERO_IMAGE_KEY,
                        "恢复默认首页大图",
                        previousImageUrl == null ? Map.of() : snapshot("imageUrl", previousImageUrl),
                        snapshot("imageUrl", null));
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
            adminOperationLogService.record(
                    adminUserId,
                    "setting_management",
                    "home_hero_update",
                    "setting",
                    setting.getId(),
                    HOME_HERO_IMAGE_KEY,
                    "更新首页大图",
                    snapshot("imageUrl", null),
                    snapshot("imageUrl", normalized));
            return;
        }

        existing.setSettingValue(normalized);
        existing.setDescription(HOME_HERO_IMAGE_DESC);
        existing.setUpdatedBy(adminUserId);
        appSettingMapper.updateById(existing);
        adminOperationLogService.record(
                adminUserId,
                "setting_management",
                "home_hero_update",
                "setting",
                existing.getId(),
                HOME_HERO_IMAGE_KEY,
                "更新首页大图",
                previousImageUrl == null ? Map.of() : snapshot("imageUrl", previousImageUrl),
                snapshot("imageUrl", normalized));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Map<String, Object> snapshot(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(key, value);
        return result;
    }
}
