package com.sheng.hikingbackend.service.impl.external;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sheng.hikingbackend.service.external.ExternalTrailProvider;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailProviderType;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StubWhitelistedExternalTrailSearchServiceImpl implements ExternalTrailProvider {

    private final ExternalTrailImportProperties properties;

    @Override
    public ExternalTrailProviderType getProviderType() {
        return ExternalTrailProviderType.STUB_WHITELIST;
    }

    @Override
    public List<ExternalTrailCandidate> search(ExternalTrailSearchRequest request) {
        ExternalTrailImportProperties.ProviderProperties provider = properties
                .getProviderOrDefault(ExternalImportProviderNames.STUB);
        if (!properties.isEnabled() || !provider.isEnabled()) {
            return List.of();
        }

        String keyword = normalizeKeyword(request);
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        return buildCatalog().stream()
                .filter(candidate -> matches(candidate, keyword))
                .limit(resolveLimit(request.getLimit()))
                .toList();
    }

    private long resolveLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return properties.getMaxCandidates();
        }
        return Math.min(requestedLimit, properties.getMaxCandidates());
    }

    private String normalizeKeyword(ExternalTrailSearchRequest request) {
        String keyword = StringUtils.hasText(request.getGeoDistrict()) ? request.getGeoDistrict()
                : StringUtils.hasText(request.getGeoCity()) ? request.getGeoCity()
                        : StringUtils.hasText(request.getLocation()) ? request.getLocation()
                                : request.getRawQuery();
        return keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
    }

    private boolean matches(ExternalTrailCandidate candidate, String keyword) {
        return candidate.getName().toLowerCase(Locale.ROOT).contains(keyword)
                || candidate.getLocation().toLowerCase(Locale.ROOT).contains(keyword)
                || candidate.getDescription().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private List<ExternalTrailCandidate> buildCatalog() {
        return List.of(
                ExternalTrailCandidate.builder()
                        .externalId("whitelist-wugongshan-east-gate-loop")
                        .sourceSite("demo.partner.trailquest.cn")
                        .sourceUrl("https://demo.partner.trailquest.cn/routes/wugongshan-east-gate-loop")
                        .imageUrl("https://demo.partner.trailquest.cn/images/wugongshan-east-gate-loop.jpg")
                        .name("武功山东江村轻装环线")
                        .location("江西 萍乡 芦溪")
                        .description("从东江村出发的轻装一日环线，适合首次体验武功山高山草甸的新手。")
                        .startLng(new BigDecimal("114.173260"))
                        .startLat(new BigDecimal("27.509540"))
                        .geoCountry("中国")
                        .geoProvince("江西")
                        .geoCity("萍乡")
                        .geoDistrict("芦溪")
                        .geoSource("whitelist_stub")
                        .difficulty("easy")
                        .difficultyLabel("简单")
                        .packType("light")
                        .durationType("single_day")
                        .distance("8 km")
                        .elevation("+620 m")
                        .duration("5h")
                        .sourceConfidence(new BigDecimal("0.92"))
                        .tags(List.of("草甸", "新手", "日出"))
                        .build(),
                ExternalTrailCandidate.builder()
                        .externalId("whitelist-mogan-morning-forest")
                        .sourceSite("whitelist.trailquest.cn")
                        .sourceUrl("https://whitelist.trailquest.cn/routes/mogan-morning-forest")
                        .imageUrl("https://whitelist.trailquest.cn/images/mogan-morning-forest.jpg")
                        .name("莫干山清晨林道徒步")
                        .location("浙江 湖州 德清")
                        .description("以森林步道和观景平台为主的轻装路线，适合周末半日或一日徒步。")
                        .startLng(new BigDecimal("119.868730"))
                        .startLat(new BigDecimal("30.592760"))
                        .geoCountry("中国")
                        .geoProvince("浙江")
                        .geoCity("湖州")
                        .geoDistrict("德清")
                        .geoSource("whitelist_stub")
                        .difficulty("easy")
                        .difficultyLabel("简单")
                        .packType("light")
                        .durationType("single_day")
                        .distance("6 km")
                        .elevation("+300 m")
                        .duration("3.5h")
                        .sourceConfidence(new BigDecimal("0.88"))
                        .tags(List.of("森林", "摄影", "新手"))
                        .build(),
                ExternalTrailCandidate.builder()
                        .externalId("whitelist-hangzhou-longjing-jiuxi-loop")
                        .sourceSite("whitelist.trailquest.cn")
                        .sourceUrl("https://whitelist.trailquest.cn/routes/hangzhou-longjing-jiuxi-loop")
                        .imageUrl("https://whitelist.trailquest.cn/images/hangzhou-longjing-jiuxi-loop.jpg")
                        .name("杭州龙井到九溪周末轻徒步环线")
                        .location("浙江 杭州 西湖")
                        .description("从龙井出发串联茶园、山脊与九溪步道的单日轻装路线，适合周末当天往返。")
                        .startLng(new BigDecimal("120.118420"))
                        .startLat(new BigDecimal("30.219510"))
                        .geoCountry("中国")
                        .geoProvince("浙江")
                        .geoCity("杭州")
                        .geoDistrict("西湖")
                        .geoSource("whitelist_stub")
                        .difficulty("easy")
                        .difficultyLabel("简单")
                        .packType("light")
                        .durationType("single_day")
                        .distance("10 km")
                        .elevation("+420 m")
                        .duration("4.5h")
                        .sourceConfidence(new BigDecimal("0.91"))
                        .tags(List.of("周末", "单日", "茶园"))
                        .build(),
                ExternalTrailCandidate.builder()
                        .externalId("whitelist-nepal-ebc-classic")
                        .sourceSite("demo.partner.trailquest.cn")
                        .sourceUrl("https://demo.partner.trailquest.cn/routes/nepal-ebc-classic")
                        .imageUrl("https://demo.partner.trailquest.cn/images/nepal-ebc-classic.jpg")
                        .name("尼泊尔 EBC 经典徒步线")
                        .location("尼泊尔 EBC")
                        .description("覆盖卢卡拉、南池和珠峰大本营的经典多日徒步路线，适合有一定高海拔经验的徒步者。")
                        .startLng(new BigDecimal("86.714760"))
                        .startLat(new BigDecimal("27.932020"))
                        .geoCountry("尼泊尔")
                        .geoProvince("Koshi")
                        .geoCity("Solukhumbu")
                        .geoDistrict("Everest")
                        .geoSource("whitelist_stub")
                        .difficulty("hard")
                        .difficultyLabel("困难")
                        .packType("heavy")
                        .durationType("multi_day")
                        .distance("65 km")
                        .elevation("+3200 m")
                        .duration("12d")
                        .sourceConfidence(new BigDecimal("0.89"))
                        .tags(List.of("高海拔", "多日", "EBC"))
                        .build(),
                ExternalTrailCandidate.builder()
                        .externalId("whitelist-dali-erhai-ridge")
                        .sourceSite("demo.partner.trailquest.cn")
                        .sourceUrl("https://demo.partner.trailquest.cn/routes/dali-erhai-ridge")
                        .imageUrl("https://demo.partner.trailquest.cn/images/dali-erhai-ridge.jpg")
                        .name("大理洱海山脊观景线")
                        .location("云南 大理")
                        .description("适合清晨或傍晚出发的观景徒步线，能同时看到苍山和洱海。")
                        .startLng(new BigDecimal("100.173060"))
                        .startLat(new BigDecimal("25.694930"))
                        .geoCountry("中国")
                        .geoProvince("云南")
                        .geoCity("大理")
                        .geoDistrict("大理市")
                        .geoSource("whitelist_stub")
                        .difficulty("moderate")
                        .difficultyLabel("适中")
                        .packType("light")
                        .durationType("single_day")
                        .distance("9 km")
                        .elevation("+540 m")
                        .duration("5.5h")
                        .sourceConfidence(new BigDecimal("0.90"))
                        .tags(List.of("日落", "观景", "摄影"))
                        .build());
    }
}
