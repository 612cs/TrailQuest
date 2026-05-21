package com.sheng.hikingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.enums.MediaBizType;
import com.sheng.hikingbackend.common.enums.MediaFileStatus;
import com.sheng.hikingbackend.common.enums.TrailReviewStatus;
import com.sheng.hikingbackend.common.enums.TrailStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.trail.CreateTrailRequest;
import com.sheng.hikingbackend.dto.trail.UpdateTrailRequest;
import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.mapper.MediaFileMapper;
import com.sheng.hikingbackend.mapper.TagMapper;
import com.sheng.hikingbackend.mapper.TrailFavoriteMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailLikeMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTagMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.service.AiTrailModerationService;
import com.sheng.hikingbackend.service.GeoService;
import com.sheng.hikingbackend.service.TrackParseService;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationDecision;
import com.sheng.hikingbackend.service.impl.ai.model.AiTrailModerationResult;
import com.sheng.hikingbackend.service.impl.support.TrackParseResult;
import com.sheng.hikingbackend.vo.geo.ReverseGeoResponse;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;

@ExtendWith(MockitoExtension.class)
class TrailServiceImplTest {

    private static final Long USER_ID = 2001L;
    private static final Long TRAIL_ID = 3001L;
    private static final Long COVER_MEDIA_ID = 4001L;

    @Mock
    private TrailMapper trailMapper;
    @Mock
    private TrailLikeMapper trailLikeMapper;
    @Mock
    private TrailFavoriteMapper trailFavoriteMapper;
    @Mock
    private MediaFileMapper mediaFileMapper;
    @Mock
    private TrailImageMapper trailImageMapper;
    @Mock
    private TrailTagMapper trailTagMapper;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private TrailTrackMapper trailTrackMapper;
    @Mock
    private AiTrailModerationService aiTrailModerationService;
    @Mock
    private GeoService geoService;
    @Mock
    private TrackParseService trackParseService;

    private TrailServiceImpl trailService;

    @BeforeEach
    void setUp() {
        trailService = new TrailServiceImpl(
                trailMapper,
                trailLikeMapper,
                trailFavoriteMapper,
                mediaFileMapper,
                trailImageMapper,
                trailTagMapper,
                tagMapper,
                trailTrackMapper,
                aiTrailModerationService,
                geoService,
                trackParseService,
                new ObjectMapper());
    }

    @Test
    void createTrailShouldApproveWhenAiModerationPasses() {
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();
        row.setReviewStatus(TrailReviewStatus.APPROVED.getCode());

        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());
        when(aiTrailModerationService.moderateTrail(any())).thenReturn(approvedModerationResult());
        doAnswer(invocation -> {
            Trail trail = invocation.getArgument(0);
            trail.setId(TRAIL_ID);
            return 1;
        }).when(trailMapper).insert(any(Trail.class));

        var result = trailService.createTrail(USER_ID, "127.0.0.5", buildCreateRequest());

        assertEquals(TRAIL_ID, result.getId());
        verify(aiTrailModerationService).moderateTrail(any());
        verify(trailMapper, org.mockito.Mockito.times(1)).updateById(any(Trail.class));
    }

    @Test
    void createTrailShouldFallbackMissingTrailFactsToPendingText() {
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();
        row.setReviewStatus(TrailReviewStatus.APPROVED.getCode());

        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());
        when(aiTrailModerationService.moderateTrail(any())).thenReturn(approvedModerationResult());
        doAnswer(invocation -> {
            Trail trail = invocation.getArgument(0);
            assertEquals("待补充", trail.getDistance());
            assertEquals("待补充", trail.getElevation());
            assertEquals("待补充", trail.getDuration());
            assertNotNull(trail.getSourceType());
            trail.setId(TRAIL_ID);
            return 1;
        }).when(trailMapper).insert(any(Trail.class));

        CreateTrailRequest request = buildCreateRequest();
        request.setDistance(" ");
        request.setElevation(null);
        request.setDuration("");

        var result = trailService.createTrail(USER_ID, "127.0.0.5", request);

        assertEquals(TRAIL_ID, result.getId());
        verify(trailMapper).insert(any(Trail.class));
    }

    @Test
    void updateTrailShouldSucceedWithin48Hours() {
        Trail trail = buildTrail(USER_ID, LocalDateTime.now().minusHours(2));
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();
        row.setReviewStatus(TrailReviewStatus.APPROVED.getCode());
        TrackParseResult parseResult = TrackParseResult.builder()
                .startLng(new BigDecimal("120.123456"))
                .startLat(new BigDecimal("30.654321"))
                .build();

        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());
        when(trackParseService.parse(any())).thenReturn(parseResult);
        when(aiTrailModerationService.moderateTrail(any())).thenReturn(approvedModerationResult());
        when(geoService.reverse(any(), any())).thenReturn(ReverseGeoResponse.builder()
                .province("江西")
                .city("萍乡")
                .district("芦溪")
                .country("中国")
                .formattedLocation("萍乡 芦溪")
                .build());

        UpdateTrailRequest request = buildUpdateRequest();
        request.setTrackMediaId(5001L);
        var result = trailService.updateTrail(TRAIL_ID, USER_ID, "127.0.0.9", request);

        assertEquals("测试路线（更新）", result.getName());
        assertEquals(Boolean.TRUE, result.getOwnedByCurrentUser());
        assertEquals(Boolean.TRUE, result.getEditableByCurrentUser());
        assertEquals("127.0.0.9", trail.getIp());
        assertEquals(new BigDecimal("120.123456"), trail.getStartLng());
        assertEquals(new BigDecimal("30.654321"), trail.getStartLat());
        assertEquals("测试路线（更新）", trail.getName());
        assertEquals(TrailReviewStatus.APPROVED.getCode(), trail.getReviewStatus());
        verify(trailMapper).updateById(trail);
        verify(trailImageMapper).deleteByTrailId(TRAIL_ID);
        verify(trailTagMapper).deleteByTrailId(TRAIL_ID);
        verify(trailTrackMapper).deleteByTrailId(TRAIL_ID);
    }

    @Test
    void updateTrailShouldRejectWhenAiModerationRejects() {
        Trail trail = buildTrail(USER_ID, LocalDateTime.now().minusHours(2));
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();
        row.setReviewStatus(TrailReviewStatus.REJECTED.getCode());

        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());
        when(aiTrailModerationService.moderateTrail(any())).thenReturn(rejectedModerationResult());

        trailService.updateTrail(TRAIL_ID, USER_ID, "127.0.0.1", buildUpdateRequest());

        assertEquals(TrailReviewStatus.REJECTED.getCode(), trail.getReviewStatus());
        assertEquals("AI 预审拒绝：包含广告导流信息（spam）", trail.getReviewRemark());
        verify(trailMapper).updateById(trail);
    }

    @Test
    void updateTrailShouldFallbackToPendingWhenAiModerationRequiresManualReview() {
        Trail trail = buildTrail(USER_ID, LocalDateTime.now().minusHours(2));
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();
        row.setReviewStatus(TrailReviewStatus.PENDING.getCode());

        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());
        when(aiTrailModerationService.moderateTrail(any())).thenReturn(manualReviewModerationResult());

        trailService.updateTrail(TRAIL_ID, USER_ID, "127.0.0.1", buildUpdateRequest());

        assertEquals(TrailReviewStatus.PENDING.getCode(), trail.getReviewStatus());
        assertEquals("AI 待人工复核：路线真实性不足，建议人工复核（credibility）", trail.getReviewRemark());
        verify(trailMapper).updateById(trail);
    }

    @Test
    void updateTrailShouldFailWhenEditWindowExpired() {
        Trail expiredTrail = buildTrail(USER_ID, LocalDateTime.now().minusHours(49));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(expiredTrail);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> trailService.updateTrail(TRAIL_ID, USER_ID, "127.0.0.1", buildUpdateRequest()));

        assertEquals("TRAIL_EDIT_EXPIRED", exception.getCode());
        verify(mediaFileMapper, never()).selectOne(any());
        verify(trailMapper, never()).updateById(any(Trail.class));
    }

    @Test
    void deleteTrailShouldRejectNonOwner() {
        Trail trail = buildTrail(USER_ID + 1, LocalDateTime.now().minusHours(1));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> trailService.deleteTrail(TRAIL_ID, USER_ID));

        assertEquals("TRAIL_EDIT_FORBIDDEN", exception.getCode());
        verify(trailMapper, never()).updateById(any(Trail.class));
    }

    @Test
    void deleteTrailShouldSoftDeleteOwnedTrail() {
        Trail trail = buildTrail(USER_ID, LocalDateTime.now().minusHours(10));
        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);

        trailService.deleteTrail(TRAIL_ID, USER_ID);

        assertEquals(TrailStatus.DELETED.getCode(), trail.getStatus());
        verify(trailMapper).updateById(trail);
    }

    @Test
    void getTrailDetailShouldThrowWhenTrailIsDeletedOrMissing() {
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> trailService.getTrailDetail(TRAIL_ID, USER_ID));

        assertEquals("TRAIL_NOT_FOUND", exception.getCode());
    }

    private Trail buildTrail(Long authorId, LocalDateTime createdAt) {
        Trail trail = new Trail();
        trail.setId(TRAIL_ID);
        trail.setAuthorId(authorId);
        trail.setCreatedAt(createdAt);
        trail.setStatus(TrailStatus.ACTIVE.getCode());
        trail.setIp("127.0.0.1");
        trail.setSourceType("user_upload");
        return trail;
    }

    private MediaFile buildMedia(Long id, String bizType, String url) {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setId(id);
        mediaFile.setUserId(USER_ID);
        mediaFile.setBizType(bizType);
        mediaFile.setStatus(MediaFileStatus.ACTIVE.getCode());
        mediaFile.setUrl(url);
        mediaFile.setOriginalName("cover.png");
        return mediaFile;
    }

    private TrailQueryRow buildTrailRow() {
        TrailQueryRow row = new TrailQueryRow();
        row.setId(TRAIL_ID);
        row.setName("测试路线（更新）");
        row.setImage("https://img.example.com/cover.png");
        row.setLocation("广东 深圳");
        row.setIp("127.0.0.9");
        row.setDifficulty("moderate");
        row.setDifficultyLabel("适中");
        row.setPackType("light");
        row.setDurationType("single_day");
        row.setDistance("12 km");
        row.setElevation("+500 m");
        row.setDuration("4h");
        row.setDescription("更新后的路线描述");
        row.setFavorites(0);
        row.setLikes(0);
        row.setAuthorId(USER_ID);
        row.setCreatedAt(LocalDateTime.now().minusHours(2));
        row.setAuthorUsername("tester");
        row.setAuthorAvatar("TS");
        row.setAuthorAvatarBg("#059669");
        row.setRating(BigDecimal.ZERO.setScale(1));
        row.setReviewCount(0);
        row.setTagsCsv("山脊,日出");
        row.setStatus(TrailStatus.ACTIVE.getCode());
        return row;
    }

    private UpdateTrailRequest buildUpdateRequest() {
        UpdateTrailRequest request = new UpdateTrailRequest();
        request.setName("测试路线（更新）");
        request.setLocation("广东 深圳");
        request.setDifficulty("moderate");
        request.setDifficultyLabel("适中");
        request.setPackType("light");
        request.setDurationType("single_day");
        request.setDistance("12 km");
        request.setElevation("+500 m");
        request.setDuration("4h");
        request.setDescription("更新后的路线描述");
        request.setCoverMediaId(COVER_MEDIA_ID);
        request.setTags(List.of("山脊", "日出"));
        request.setGalleryMediaIds(List.of());
        request.setTrackMediaId(null);
        return request;
    }

    private CreateTrailRequest buildCreateRequest() {
        CreateTrailRequest request = new CreateTrailRequest();
        request.setName("测试路线（创建）");
        request.setLocation("广东 深圳");
        request.setDifficulty("moderate");
        request.setDifficultyLabel("适中");
        request.setPackType("light");
        request.setDurationType("single_day");
        request.setDistance("8 km");
        request.setElevation("+260 m");
        request.setDuration("3h");
        request.setDescription("创建时的路线描述");
        request.setCoverMediaId(COVER_MEDIA_ID);
        request.setTags(List.of("森林", "新手"));
        request.setGalleryMediaIds(List.of());
        request.setTrackMediaId(null);
        return request;
    }

    private AiTrailModerationResult approvedModerationResult() {
        return AiTrailModerationResult.builder()
                .decision(AiTrailModerationDecision.APPROVED)
                .riskLevel("low")
                .riskCategories(List.of())
                .reason("AI 预审通过")
                .modelName("qwen-plus")
                .fallbackByError(false)
                .build();
    }

    private AiTrailModerationResult rejectedModerationResult() {
        return AiTrailModerationResult.builder()
                .decision(AiTrailModerationDecision.REJECTED)
                .riskLevel("high")
                .riskCategories(List.of("spam"))
                .reason("包含广告导流信息")
                .modelName("qwen-plus")
                .fallbackByError(false)
                .build();
    }

    private AiTrailModerationResult manualReviewModerationResult() {
        return AiTrailModerationResult.builder()
                .decision(AiTrailModerationDecision.NEEDS_MANUAL_REVIEW)
                .riskLevel("medium")
                .riskCategories(List.of("credibility"))
                .reason("路线真实性不足，建议人工复核")
                .modelName("qwen-plus")
                .fallbackByError(false)
                .build();
    }
}
