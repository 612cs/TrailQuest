package com.sheng.hikingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.sheng.hikingbackend.common.enums.TrailStatus;
import com.sheng.hikingbackend.common.exception.BusinessException;
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
import com.sheng.hikingbackend.service.TrackParseService;
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
                trackParseService,
                new ObjectMapper());
    }

    @Test
    void updateTrailShouldSucceedWithin48Hours() {
        Trail trail = buildTrail(USER_ID, LocalDateTime.now().minusHours(2));
        MediaFile coverMedia = buildMedia(COVER_MEDIA_ID, MediaBizType.TRAIL_COVER.getCode(), "https://img.example.com/cover.png");
        TrailQueryRow row = buildTrailRow();

        when(trailMapper.selectActiveById(TRAIL_ID)).thenReturn(trail);
        when(mediaFileMapper.selectOne(any())).thenReturn(coverMedia);
        when(trailMapper.selectTrailDetailById(TRAIL_ID, USER_ID)).thenReturn(row);
        when(trailImageMapper.selectByTrailId(TRAIL_ID)).thenReturn(List.of());

        UpdateTrailRequest request = buildUpdateRequest();
        var result = trailService.updateTrail(TRAIL_ID, USER_ID, "127.0.0.9", request);

        assertEquals("测试路线（更新）", result.getName());
        assertEquals(Boolean.TRUE, result.getOwnedByCurrentUser());
        assertEquals(Boolean.TRUE, result.getEditableByCurrentUser());
        assertEquals("127.0.0.9", trail.getIp());
        assertEquals("测试路线（更新）", trail.getName());
        verify(trailMapper).updateById(trail);
        verify(trailImageMapper).deleteByTrailId(TRAIL_ID);
        verify(trailTagMapper).deleteByTrailId(TRAIL_ID);
        verify(trailTrackMapper).deleteByTrailId(TRAIL_ID);
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
}
