package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.PageResponse;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminOperationLogPageRequest;
import com.sheng.hikingbackend.entity.AdminOperationLog;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.AdminOperationLogMapper;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.AdminOperationLogService;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogDetailVo;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogListItemVo;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogQueryRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOperationLogServiceImpl implements AdminOperationLogService {

    private static final String RESULT_STATUS_SUCCESS = "success";

    private final AdminOperationLogMapper adminOperationLogMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void record(
            Long operatorId,
            String moduleCode,
            String actionCode,
            String targetType,
            Long targetId,
            String targetTitle,
            String reason,
            Map<String, Object> beforeSnapshot,
            Map<String, Object> afterSnapshot) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null) {
            throw BusinessException.notFound("ADMIN_OPERATOR_NOT_FOUND", "操作管理员不存在");
        }

        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operator.getUsername());
        log.setOperatorRole(operator.getRole() == null ? null : operator.getRole().name());
        log.setModuleCode(moduleCode);
        log.setActionCode(actionCode);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetTitle(normalize(targetTitle));
        log.setReason(normalize(reason));
        log.setResultStatus(RESULT_STATUS_SUCCESS);
        log.setBeforeSnapshot(writeJson(beforeSnapshot));
        log.setAfterSnapshot(writeJson(afterSnapshot));
        log.setCreatedAt(LocalDateTime.now());
        adminOperationLogMapper.insert(log);
    }

    @Override
    public PageResponse<AdminOperationLogListItemVo> pageLogs(AdminOperationLogPageRequest request) {
        Page<AdminOperationLogQueryRow> page = Page.of(request.getPageNum(), request.getPageSize());
        IPage<AdminOperationLogQueryRow> result = adminOperationLogMapper.selectAdminOperationLogPage(page, request);
        return PageResponse.of(result.getRecords().stream()
                .map(this::toListItemVo)
                .toList(), result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public AdminOperationLogDetailVo getLogDetail(Long logId) {
        AdminOperationLogQueryRow row = adminOperationLogMapper.selectAdminOperationLogDetailById(logId);
        if (row == null) {
            throw BusinessException.notFound("ADMIN_OPERATION_LOG_NOT_FOUND", "操作日志不存在");
        }
        return AdminOperationLogDetailVo.builder()
                .id(row.getId())
                .operatorId(row.getOperatorId())
                .operatorName(row.getOperatorName())
                .operatorRole(row.getOperatorRole())
                .moduleCode(row.getModuleCode())
                .actionCode(row.getActionCode())
                .targetType(row.getTargetType())
                .targetId(row.getTargetId())
                .targetTitle(row.getTargetTitle())
                .reason(row.getReason())
                .resultStatus(row.getResultStatus())
                .beforeSnapshot(parseJson(row.getBeforeSnapshot()))
                .afterSnapshot(parseJson(row.getAfterSnapshot()))
                .requestId(row.getRequestId())
                .ipAddress(row.getIpAddress())
                .userAgent(row.getUserAgent())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminOperationLogListItemVo toListItemVo(AdminOperationLogQueryRow row) {
        return AdminOperationLogListItemVo.builder()
                .id(row.getId())
                .operatorId(row.getOperatorId())
                .operatorName(row.getOperatorName())
                .operatorRole(row.getOperatorRole())
                .moduleCode(row.getModuleCode())
                .actionCode(row.getActionCode())
                .targetType(row.getTargetType())
                .targetId(row.getTargetId())
                .targetTitle(row.getTargetTitle())
                .reason(row.getReason())
                .resultStatus(row.getResultStatus())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String writeJson(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw BusinessException.badRequest("ADMIN_OPERATION_LOG_SERIALIZE_FAILED", "操作日志序列化失败");
        }
    }

    private Map<String, Object> parseJson(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw BusinessException.badRequest("ADMIN_OPERATION_LOG_PARSE_FAILED", "操作日志解析失败");
        }
    }
}
