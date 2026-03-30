package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.trail.CreateTrailRequest;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.dto.trail.UpdateTrailRequest;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;

public interface TrailService {

    PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request, Long currentUserId);

    TrailDetailVo getTrailDetail(Long id, Long currentUserId);

    TrailDetailVo createTrail(Long currentUserId, String requestIp, CreateTrailRequest request);

    TrailDetailVo updateTrail(Long trailId, Long currentUserId, String requestIp, UpdateTrailRequest request);

    TrailDetailVo getTrailDetailForAdmin(Long id);

    void deleteTrail(Long trailId, Long currentUserId);

    TrailInteractionVo likeTrail(Long trailId, Long currentUserId);

    TrailInteractionVo unlikeTrail(Long trailId, Long currentUserId);

    TrailInteractionVo favoriteTrail(Long trailId, Long currentUserId);

    TrailInteractionVo unfavoriteTrail(Long trailId, Long currentUserId);

    TrailDetailVo toTrailDetailVo(TrailQueryRow row);

    String formatPublishTime(java.time.LocalDateTime createdAt);
}
