package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;

public interface LandscapeContextService {

    LandscapeContext build(Long trailId, int days);
}
