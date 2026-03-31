package com.sheng.hikingbackend.service;

import java.util.Map;

import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.dto.admin.AdminOperationLogPageRequest;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogListItemVo;

public interface AdminOperationLogService {

    void record(
            Long operatorId,
            String moduleCode,
            String actionCode,
            String targetType,
            Long targetId,
            String targetTitle,
            String reason,
            Map<String, Object> beforeSnapshot,
            Map<String, Object> afterSnapshot);

    PageResponse<AdminOperationLogListItemVo> pageLogs(AdminOperationLogPageRequest request);

    AdminOperationLogDetailVo getLogDetail(Long logId);
}
