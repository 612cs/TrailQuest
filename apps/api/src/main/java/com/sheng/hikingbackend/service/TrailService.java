package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;

public interface TrailService {

    PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request, Long currentUserId);

    TrailDetailVo getTrailDetail(Long id, Long currentUserId);

    TrailInteractionVo likeTrail(Long trailId, Long currentUserId);

    TrailInteractionVo unlikeTrail(Long trailId, Long currentUserId);

    TrailInteractionVo favoriteTrail(Long trailId, Long currentUserId);

    TrailInteractionVo unfavoriteTrail(Long trailId, Long currentUserId);
}
