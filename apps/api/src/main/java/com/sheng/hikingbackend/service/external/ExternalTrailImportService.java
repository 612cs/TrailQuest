package com.sheng.hikingbackend.service.external;

import com.sheng.hikingbackend.service.external.model.ExternalTrailImportResult;
import com.sheng.hikingbackend.service.external.model.ExternalTrailSearchRequest;

public interface ExternalTrailImportService {

    ExternalTrailImportResult searchAndImport(ExternalTrailSearchRequest request);
}
