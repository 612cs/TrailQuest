package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.entity.MediaFile;
import com.sheng.hikingbackend.service.impl.support.TrackParseResult;

public interface TrackParseService {

    TrackParseResult parse(MediaFile mediaFile);
}
