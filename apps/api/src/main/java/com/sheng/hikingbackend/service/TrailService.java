package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;

public interface TrailService {

    PageResponse<TrailDetailVo> pageTrails(TrailPageRequest request);

    TrailDetailVo getTrailDetail(Long id);
}
