package com.sheng.hikingbackend.service.external;

import com.sheng.hikingbackend.service.external.model.TrailImportModerationContext;
import com.sheng.hikingbackend.service.external.model.TrailImportModerationResult;

public interface TrailImportModerationTrigger {

    TrailImportModerationResult moderate(TrailImportModerationContext context);
}
