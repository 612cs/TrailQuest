package com.sheng.hikingbackend.service.landscape;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.service.landscape.model.LightPollutionContext;

@Service
public class LightPollutionServiceImpl implements LightPollutionService {

    @Override
    public LightPollutionContext resolve(BigDecimal lng, BigDecimal lat, String locationText) {
        double level = 3.2;
        if (StringUtils.hasText(locationText)) {
            String normalized = locationText.trim();
            if (containsAny(normalized, "北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "南京", "武汉", "西安")) {
                level = 4.4;
            } else if (containsAny(normalized, "山", "峰", "岭", "林场", "保护区", "草原", "国家公园")) {
                level = 2.2;
            }
        }
        return LightPollutionContext.builder()
                .ready(false)
                .level(level)
                .source("fallback-heuristic")
                .build();
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
