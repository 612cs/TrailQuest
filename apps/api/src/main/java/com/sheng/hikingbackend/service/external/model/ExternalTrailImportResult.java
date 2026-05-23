package com.sheng.hikingbackend.service.external.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExternalTrailImportResult {

    private List<ExternalTrailImportItem> items;
    private List<TrailImportLogEntry> importLogs;
}
