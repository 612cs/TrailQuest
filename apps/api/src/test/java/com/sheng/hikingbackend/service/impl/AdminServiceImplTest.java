package com.sheng.hikingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminBanUserRequest;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.AdminOperationLogMapper;
import com.sheng.hikingbackend.mapper.ReviewMapper;
import com.sheng.hikingbackend.mapper.TrailImageMapper;
import com.sheng.hikingbackend.mapper.TrailMapper;
import com.sheng.hikingbackend.mapper.TrailTrackMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AdminOperationLogService;
import com.sheng.hikingbackend.service.ReviewService;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    private static final Long ADMIN_ID = 9001L;
    private static final Long USER_ID = 2001L;
    private static final Long TRAIL_ID = 3001L;

    @Mock
    private TrailMapper trailMapper;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private ReviewService reviewService;
    @Mock
    private TrailImageMapper trailImageMapper;
    @Mock
    private TrailTrackMapper trailTrackMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AdminOperationLogMapper adminOperationLogMapper;
    @Mock
    private AdminOperationLogService adminOperationLogService;

    private AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        adminService = new AdminServiceImpl(
                trailMapper,
                reviewMapper,
                reviewService,
                trailImageMapper,
                trailTrackMapper,
                userMapper,
                adminOperationLogMapper,
                adminOperationLogService);
    }

    @Test
    void banUserShouldUpdateStatusAndWriteLog() {
        User user = buildUser("active");
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        AdminBanUserRequest request = new AdminBanUserRequest();
        request.setReason("发布违规内容");

        adminService.banUser(USER_ID, ADMIN_ID, request);

        assertEquals("banned", user.getStatus());
        assertEquals("发布违规内容", user.getBanReason());
        assertEquals(ADMIN_ID, user.getBannedBy());
        assertNotNull(user.getBannedAt());
        verify(userMapper).updateById(user);
        verify(adminOperationLogService).record(
                eq(ADMIN_ID),
                eq("user_management"),
                eq("user_ban"),
                eq("user"),
                eq(USER_ID),
                eq("hiker"),
                eq("发布违规内容"),
                any(Map.class),
                any(Map.class));
    }

    @Test
    void unbanUserShouldResetStatusAndWriteLog() {
        User user = buildUser("banned");
        user.setBanReason("恶意灌水");
        user.setBannedBy(ADMIN_ID);
        user.setBannedAt(LocalDateTime.now().minusDays(1));
        when(userMapper.selectById(USER_ID)).thenReturn(user);

        adminService.unbanUser(USER_ID, ADMIN_ID);

        assertEquals("active", user.getStatus());
        assertEquals(null, user.getBanReason());
        assertEquals(null, user.getBannedBy());
        assertEquals(null, user.getBannedAt());
        verify(userMapper).updateById(user);
        verify(adminOperationLogService).record(
                eq(ADMIN_ID),
                eq("user_management"),
                eq("user_unban"),
                eq("user"),
                eq(USER_ID),
                eq("hiker"),
                eq("恢复账号"),
                any(Map.class),
                any(Map.class));
    }

    @Test
    void banUserShouldRejectSelfBan() {
        User user = buildUser("active");
        when(userMapper.selectById(ADMIN_ID)).thenReturn(user);

        AdminBanUserRequest request = new AdminBanUserRequest();
        request.setReason("误操作");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminService.banUser(ADMIN_ID, ADMIN_ID, request));

        assertEquals("ADMIN_SELF_BAN_FORBIDDEN", exception.getCode());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void offlineTrailShouldSoftDeleteAndWriteLog() {
        Trail trail = buildTrail("active", "approved");
        when(trailMapper.selectById(TRAIL_ID)).thenReturn(trail);

        adminService.offlineTrail(TRAIL_ID, ADMIN_ID);

        assertEquals("deleted", trail.getStatus());
        verify(trailMapper).updateById(trail);
        verify(adminOperationLogService).record(
                eq(ADMIN_ID),
                eq("trail_management"),
                eq("trail_offline"),
                eq("trail"),
                eq(TRAIL_ID),
                eq("Sunrise Trail"),
                eq("路线下架"),
                any(Map.class),
                any(Map.class));
    }

    @Test
    void restoreTrailShouldRecoverStatusAndWriteLog() {
        Trail trail = buildTrail("deleted", "approved");
        when(trailMapper.selectById(TRAIL_ID)).thenReturn(trail);

        adminService.restoreTrail(TRAIL_ID, ADMIN_ID);

        assertEquals("active", trail.getStatus());
        verify(trailMapper).updateById(trail);
        verify(adminOperationLogService).record(
                eq(ADMIN_ID),
                eq("trail_management"),
                eq("trail_restore"),
                eq("trail"),
                eq(TRAIL_ID),
                eq("Sunrise Trail"),
                eq("路线恢复"),
                any(Map.class),
                any(Map.class));
    }

    @Test
    void restoreTrailShouldRejectNonOfflineTrail() {
        Trail trail = buildTrail("active", "approved");
        when(trailMapper.selectById(TRAIL_ID)).thenReturn(trail);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminService.restoreTrail(TRAIL_ID, ADMIN_ID));

        assertEquals("TRAIL_NOT_OFFLINE", exception.getCode());
    }

    @Test
    void offlineTrailShouldRejectUnapprovedTrail() {
        Trail trail = buildTrail("active", "pending");
        when(trailMapper.selectById(TRAIL_ID)).thenReturn(trail);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminService.offlineTrail(TRAIL_ID, ADMIN_ID));

        assertEquals("TRAIL_NOT_APPROVED", exception.getCode());
        verify(adminOperationLogService, never()).record(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private User buildUser(String status) {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("hiker");
        user.setEmail("hiker@example.com");
        user.setStatus(status);
        return user;
    }

    private Trail buildTrail(String status, String reviewStatus) {
        Trail trail = new Trail();
        trail.setId(TRAIL_ID);
        trail.setName("Sunrise Trail");
        trail.setStatus(status);
        trail.setReviewStatus(reviewStatus);
        trail.setCreatedAt(LocalDateTime.now().minusHours(8));
        return trail;
    }
}
