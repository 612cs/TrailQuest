package com.sheng.hikingbackend.service.impl.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.external.ExternalTrailSearchService;
import com.sheng.hikingbackend.service.external.TrailImportDeduplicationService;
import com.sheng.hikingbackend.service.external.TrailImportModerationTrigger;
import com.sheng.hikingbackend.service.external.model.ExternalTrailCandidate;
import com.sheng.hikingbackend.service.external.model.ExternalTrailImportResult;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;
import com.sheng.hikingbackend.service.external.model.TrailDeduplicationMatch;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationDecision;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationResult;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;

@ExtendWith(MockitoExtension.class)
class ExternalTrailImportServiceImplTest {

    @Mock
    private ExternalTrailSearchService externalTrailSearchService;
    @Mock
    private TrailImportDeduplicationService trailImportDeduplicationService;
    @Mock
    private TrailImportModerationTrigger trailImportModerationTrigger;
    @Mock
    private TrailMapper trailMapper;
    @Mock
    private GeoService geoService;

    private ExternalTrailImportServiceImpl externalTrailImportService;

    @BeforeEach
    void setUp() {
        ExternalTrailImportProperties properties = new ExternalTrailImportProperties();
        properties.setFallbackAuthorId(99L);
        externalTrailImportService = new ExternalTrailImportServiceImpl(
                externalTrailSearchService,
                trailImportDeduplicationService,
                trailImportModerationTrigger,
                new ExternalTrailCandidateValidator(),
                properties,
                trailMapper,
                geoService,
                new ObjectMapper());
    }

    @Test
    void searchAndImportShouldCreateConversationVisibleImportedTrail() {
        ExternalTrailCandidate candidate = ExternalTrailCandidate.builder()
                .externalId("wugongshan-demo")
                .sourceSite("demo.partner.trailquest.cn")
                .sourceUrl("https://demo.partner.trailquest.cn/routes/wugongshan-demo")
                .imageUrl("https://demo.partner.trailquest.cn/images/wugongshan-demo.jpg")
                .name("武功山草甸体验线")
                .location("江西 萍乡")
                .description("适合新手体验高山草甸景观的轻装路线。")
                .startLng(new BigDecimal("114.173260"))
                .startLat(new BigDecimal("27.509540"))
                .difficulty("easy")
                .difficultyLabel("简单")
                .packType("light")
                .durationType("single_day")
                .distance("8 km")
                .elevation("+600 m")
                .duration("5h")
                .sourceConfidence(new BigDecimal("0.90"))
                .build();

        when(externalTrailSearchService.search(any())).thenReturn(List.of(candidate));
        when(geoService.reverse(candidate.getStartLng(), candidate.getStartLat())).thenReturn(ReverseGeoResponse.builder()
                .country("中国")
                .province("江西")
                .city("萍乡")
                .district("芦溪")
                .formattedLocation("江西 萍乡 芦溪")
                .build());
        when(trailImportDeduplicationService.match(any())).thenReturn(TrailDeduplicationMatch.notMatched());
        when(trailImportModerationTrigger.moderate(any())).thenReturn(TrailImportModerationResult.builder()
                .decision(TrailImportModerationDecision.APPROVED)
                .reviewStatusToPersist("pending")
                .aiReviewStatusToPersist("approved")
                .riskLevel("low")
                .reason("白名单来源且字段完整，可在当前对话返回，公开展示仍需人工复核")
                .traceId("trace-1")
                .reviewedAt(java.time.LocalDateTime.now())
                .categories(List.of("whitelist_source"))
                .conversationVisible(true)
                .publiclyVisible(false)
                .build());

        ExternalTrailImportResult result = externalTrailImportService.searchAndImport(ExternalTrailSearchRequest.builder()
                .rawQuery("武功山新手路线")
                .geoCity("萍乡")
                .requesterUserId(1001L)
                .limit(2)
                .build());

        assertEquals(1, result.getItems().size());
        assertTrue(result.getItems().get(0).isCreated());
        assertTrue(result.getItems().get(0).isConversationVisible());
        assertFalse(result.getItems().get(0).isPubliclyVisible());
        assertEquals("approved", result.getItems().get(0).getModerationResult().getAiReviewStatusToPersist());

        ArgumentCaptor<Trail> insertedTrail = ArgumentCaptor.forClass(Trail.class);
        verify(trailMapper).insert(insertedTrail.capture());
        assertEquals("ai_web_import", insertedTrail.getValue().getSourceType());
        assertEquals("pending", insertedTrail.getValue().getReviewStatus());
        assertEquals("0.0.0.0", insertedTrail.getValue().getIp());
        assertEquals(1001L, insertedTrail.getValue().getAuthorId());

        ArgumentCaptor<Trail> updatedTrail = ArgumentCaptor.forClass(Trail.class);
        verify(trailMapper).updateById(updatedTrail.capture());
        assertEquals("approved", updatedTrail.getValue().getAiReviewStatus());
        assertEquals("pending", updatedTrail.getValue().getReviewStatus());
    }

    @Test
    void searchAndImportShouldSkipIncompleteCandidateBeforeInsert() {
        ExternalTrailCandidate incomplete = ExternalTrailCandidate.builder()
                .externalId("bad-demo")
                .sourceSite("demo.partner.trailquest.cn")
                .sourceUrl("https://demo.partner.trailquest.cn/routes/bad-demo")
                .name("字段不完整路线")
                .location("江西 萍乡")
                .description("缺少图片和坐标")
                .build();

        when(externalTrailSearchService.search(any())).thenReturn(List.of(incomplete));
        when(geoService.lookupLocation("江西 萍乡")).thenReturn(null);
        when(trailImportDeduplicationService.match(any())).thenReturn(TrailDeduplicationMatch.notMatched());
        when(trailImportModerationTrigger.moderate(any())).thenReturn(TrailImportModerationResult.builder()
                .decision(TrailImportModerationDecision.APPROVED)
                .reviewStatusToPersist("pending")
                .aiReviewStatusToPersist("approved")
                .riskLevel("low")
                .reason("白名单来源且字段完整")
                .traceId("trace-2")
                .reviewedAt(java.time.LocalDateTime.now())
                .categories(List.of("whitelist_source"))
                .conversationVisible(true)
                .publiclyVisible(false)
                .build());

        ExternalTrailImportResult result = externalTrailImportService.searchAndImport(ExternalTrailSearchRequest.builder()
                .rawQuery("江西 萍乡")
                .location("江西 萍乡")
                .build());

        assertEquals(1, result.getItems().size());
        verify(trailMapper).insert(any(Trail.class));
        verify(trailImportModerationTrigger).moderate(any());
    }

    @Test
    void searchAndImportShouldSkipMismatchedLocationCandidate() {
        ExternalTrailCandidate mismatch = ExternalTrailCandidate.builder()
                .externalId("wutaishan-demo")
                .sourceSite("demo.partner.trailquest.cn")
                .sourceUrl("https://demo.partner.trailquest.cn/routes/wutaishan-demo")
                .imageUrl("https://demo.partner.trailquest.cn/images/wutaishan-demo.jpg")
                .name("五台山大朝台")
                .location("山西 忻州 五台山")
                .description("五台山大朝台徒步")
                .startLng(new BigDecimal("113.000000"))
                .startLat(new BigDecimal("38.500000"))
                .build();

        when(externalTrailSearchService.search(any())).thenReturn(List.of(mismatch));

        ExternalTrailImportResult result = externalTrailImportService.searchAndImport(ExternalTrailSearchRequest.builder()
                .rawQuery("尼泊尔EBC")
                .location("尼泊尔")
                .build());

        assertTrue(result.getItems().isEmpty());
        assertEquals(1, result.getImportLogs().size());
        assertEquals("候选地点与查询地点不一致，已跳过", result.getImportLogs().get(0).getMessage());
        verify(trailMapper, never()).insert(any(Trail.class));
        verify(trailImportModerationTrigger, never()).moderate(any());
    }

    @Test
    void searchAndImportShouldFallbackToConfiguredAuthorWhenRequesterMissing() {
        ExternalTrailCandidate candidate = ExternalTrailCandidate.builder()
                .externalId("fallback-author-demo")
                .sourceSite("demo.partner.trailquest.cn")
                .sourceUrl("https://demo.partner.trailquest.cn/routes/fallback-author-demo")
                .imageUrl("https://demo.partner.trailquest.cn/images/fallback-author-demo.jpg")
                .name("默认作者导入路线")
                .location("江西 萍乡")
                .description("验证默认作者兜底")
                .startLng(new BigDecimal("114.173260"))
                .startLat(new BigDecimal("27.509540"))
                .distance("8 km")
                .elevation("+600 m")
                .duration("5h")
                .build();

        when(externalTrailSearchService.search(any())).thenReturn(List.of(candidate));
        when(geoService.reverse(candidate.getStartLng(), candidate.getStartLat())).thenReturn(ReverseGeoResponse.builder()
                .country("中国")
                .province("江西")
                .city("萍乡")
                .district("芦溪")
                .formattedLocation("江西 萍乡 芦溪")
                .build());
        when(trailImportDeduplicationService.match(any())).thenReturn(TrailDeduplicationMatch.notMatched());
        when(trailImportModerationTrigger.moderate(any())).thenReturn(TrailImportModerationResult.builder()
                .decision(TrailImportModerationDecision.APPROVED)
                .reviewStatusToPersist("pending")
                .aiReviewStatusToPersist("approved")
                .riskLevel("low")
                .reason("默认作者兜底")
                .traceId("trace-fallback-author")
                .reviewedAt(java.time.LocalDateTime.now())
                .categories(List.of("whitelist_source"))
                .conversationVisible(true)
                .publiclyVisible(false)
                .build());

        externalTrailImportService.searchAndImport(ExternalTrailSearchRequest.builder()
                .rawQuery("江西 萍乡")
                .location("江西 萍乡")
                .build());

        ArgumentCaptor<Trail> insertedTrail = ArgumentCaptor.forClass(Trail.class);
        verify(trailMapper).insert(insertedTrail.capture());
        assertEquals(99L, insertedTrail.getValue().getAuthorId());
    }
}
