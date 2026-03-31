package com.sheng.hikingbackend.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.dto.admin.AdminCreateSystemOptionItemRequest;
import com.sheng.hikingbackend.dto.admin.AdminUpdateSystemOptionItemRequest;
import com.sheng.hikingbackend.entity.SystemOptionGroup;
import com.sheng.hikingbackend.entity.SystemOptionItem;
import com.sheng.hikingbackend.mapper.SystemOptionGroupMapper;
import com.sheng.hikingbackend.mapper.SystemOptionItemMapper;
import com.sheng.hikingbackend.service.AdminOperationLogService;
import com.sheng.hikingbackend.service.OptionConfigService;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionGroupVo;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionItemVo;
import com.sheng.hikingbackend.vo.config.PublicSystemOptionItemVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionConfigServiceImpl implements OptionConfigService {

    private static final String GROUP_STATUS_ACTIVE = "active";
    private static final Set<String> CREATE_ALLOWED_GROUPS = Set.of("home_activity");
    private static final Set<String> ALLOWED_ICON_NAMES = Set.of(
            "Accessibility",
            "Activity",
            "Backpack",
            "Bike",
            "Compass",
            "Footprints",
            "Map",
            "Mountain",
            "Package2",
            "PawPrint",
            "Route",
            "Scale",
            "Sprout",
            "Trees");

    private final SystemOptionGroupMapper groupMapper;
    private final SystemOptionItemMapper itemMapper;
    private final AdminOperationLogService adminOperationLogService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, List<PublicSystemOptionItemVo>> getPublicOptions(List<String> groupCodes) {
        List<SystemOptionGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<SystemOptionGroup>()
                .eq(SystemOptionGroup::getStatus, GROUP_STATUS_ACTIVE)
                .in(groupCodes != null && !groupCodes.isEmpty(), SystemOptionGroup::getGroupCode, groupCodes)
                .orderByAsc(SystemOptionGroup::getId));

        if (groups.isEmpty()) {
            return Map.of();
        }

        Map<Long, SystemOptionGroup> groupById = groups.stream()
                .collect(Collectors.toMap(SystemOptionGroup::getId, group -> group, (left, right) -> left, LinkedHashMap::new));

        List<SystemOptionItem> items = itemMapper.selectList(new LambdaQueryWrapper<SystemOptionItem>()
                .in(SystemOptionItem::getGroupId, groupById.keySet())
                .eq(SystemOptionItem::getEnabled, Boolean.TRUE)
                .orderByAsc(SystemOptionItem::getSortOrder, SystemOptionItem::getId));

        Map<String, List<PublicSystemOptionItemVo>> result = new LinkedHashMap<>();
        groups.forEach(group -> result.put(group.getGroupCode(), List.of()));

        Map<Long, List<PublicSystemOptionItemVo>> groupedItems = items.stream()
                .collect(Collectors.groupingBy(SystemOptionItem::getGroupId, LinkedHashMap::new,
                        Collectors.mapping(this::toPublicItemVo, Collectors.toList())));

        groups.forEach(group -> result.put(group.getGroupCode(), groupedItems.getOrDefault(group.getId(), List.of())));
        return result;
    }

    @Override
    public List<AdminSystemOptionGroupVo> getAdminGroups() {
        List<SystemOptionGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<SystemOptionGroup>()
                .orderByAsc(SystemOptionGroup::getId));
        if (groups.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> countByGroupId = itemMapper.selectList(new LambdaQueryWrapper<SystemOptionItem>()
                .in(SystemOptionItem::getGroupId, groups.stream().map(SystemOptionGroup::getId).toList()))
                .stream()
                .collect(Collectors.groupingBy(SystemOptionItem::getGroupId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        return groups.stream()
                .map(group -> AdminSystemOptionGroupVo.builder()
                        .groupCode(group.getGroupCode())
                        .groupName(group.getGroupName())
                        .description(group.getDescription())
                        .status(group.getStatus())
                        .itemCount(countByGroupId.getOrDefault(group.getId(), 0))
                        .allowCreate(CREATE_ALLOWED_GROUPS.contains(group.getGroupCode()))
                        .build())
                .toList();
    }

    @Override
    public List<AdminSystemOptionItemVo> getAdminGroupItems(String groupCode) {
        SystemOptionGroup group = requireGroup(groupCode);
        List<SystemOptionItem> items = itemMapper.selectList(new LambdaQueryWrapper<SystemOptionItem>()
                .eq(SystemOptionItem::getGroupId, group.getId())
                .orderByAsc(SystemOptionItem::getSortOrder, SystemOptionItem::getId));
        return items.stream().map(this::toAdminItemVo).toList();
    }

    @Override
    @Transactional
    public AdminSystemOptionItemVo createAdminGroupItem(String groupCode, Long adminUserId, AdminCreateSystemOptionItemRequest request) {
        SystemOptionGroup group = requireGroup(groupCode);
        if (!CREATE_ALLOWED_GROUPS.contains(groupCode)) {
            throw BusinessException.badRequest("OPTION_GROUP_CREATE_FORBIDDEN", "当前分组暂不支持新增配置项");
        }

        String itemCode = normalizeRequired(request.getItemCode(), "配置项编码不能为空");
        ensureValidCode(itemCode);
        validateIconName(request.getIconName());
        ensureItemCodeUnique(group.getId(), itemCode, null);

        SystemOptionItem item = new SystemOptionItem();
        item.setGroupId(group.getId());
        item.setItemCode(itemCode);
        applyCommonFields(item, request.getItemLabel(), request.getItemSubLabel(), request.getDescription(), request.getIconName(),
                request.getSortOrder(), request.getEnabled(), request.getExtra());
        item.setBuiltin(Boolean.FALSE);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.insert(item);

        adminOperationLogService.record(
                adminUserId,
                "config_center",
                "option_item_create",
                "option_item",
                item.getId(),
                item.getItemLabel(),
                "创建配置项",
                Map.of(),
                buildSnapshot(groupCode, item));
        return toAdminItemVo(item);
    }

    @Override
    @Transactional
    public AdminSystemOptionItemVo updateAdminGroupItem(String groupCode, Long itemId, Long adminUserId, AdminUpdateSystemOptionItemRequest request) {
        SystemOptionGroup group = requireGroup(groupCode);
        SystemOptionItem item = requireItem(group.getId(), itemId);
        Map<String, Object> beforeSnapshot = buildSnapshot(groupCode, item);
        boolean enabledChanged = !item.getEnabled().equals(Boolean.TRUE.equals(request.getEnabled()));

        validateIconName(request.getIconName());
        applyCommonFields(item, request.getItemLabel(), request.getItemSubLabel(), request.getDescription(), request.getIconName(),
                request.getSortOrder(), request.getEnabled(), request.getExtra());
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);

        adminOperationLogService.record(
                adminUserId,
                "config_center",
                enabledChanged ? "option_item_toggle" : "option_item_update",
                "option_item",
                item.getId(),
                item.getItemLabel(),
                enabledChanged ? "切换配置项启用状态" : "更新配置项",
                beforeSnapshot,
                buildSnapshot(groupCode, item));
        return toAdminItemVo(item);
    }

    private void applyCommonFields(
            SystemOptionItem item,
            String itemLabel,
            String itemSubLabel,
            String description,
            String iconName,
            Integer sortOrder,
            Boolean enabled,
            Map<String, Object> extra) {
        item.setItemLabel(normalizeRequired(itemLabel, "配置项名称不能为空"));
        item.setItemSubLabel(normalizeOptional(itemSubLabel));
        item.setDescription(normalizeOptional(description));
        item.setIconName(normalizeOptional(iconName));
        item.setSortOrder(sortOrder);
        item.setEnabled(Boolean.TRUE.equals(enabled));
        item.setExtraJson(writeExtraJson(extra));
    }

    private SystemOptionGroup requireGroup(String groupCode) {
        SystemOptionGroup group = groupMapper.selectOne(new LambdaQueryWrapper<SystemOptionGroup>()
                .eq(SystemOptionGroup::getGroupCode, groupCode)
                .last("LIMIT 1"));
        if (group == null) {
            throw BusinessException.notFound("OPTION_GROUP_NOT_FOUND", "配置分组不存在");
        }
        return group;
    }

    private SystemOptionItem requireItem(Long groupId, Long itemId) {
        SystemOptionItem item = itemMapper.selectById(itemId);
        if (item == null || !groupId.equals(item.getGroupId())) {
            throw BusinessException.notFound("OPTION_ITEM_NOT_FOUND", "配置项不存在");
        }
        return item;
    }

    private void ensureItemCodeUnique(Long groupId, String itemCode, Long excludeItemId) {
        List<SystemOptionItem> matches = itemMapper.selectList(new LambdaQueryWrapper<SystemOptionItem>()
                .eq(SystemOptionItem::getGroupId, groupId)
                .eq(SystemOptionItem::getItemCode, itemCode));
        boolean exists = matches.stream().anyMatch(item -> excludeItemId == null || !excludeItemId.equals(item.getId()));
        if (exists) {
            throw BusinessException.badRequest("OPTION_ITEM_CODE_DUPLICATE", "同一分组下配置项编码不能重复");
        }
    }

    private void validateIconName(String iconName) {
        String normalized = normalizeOptional(iconName);
        if (normalized == null) {
            return;
        }
        if (!ALLOWED_ICON_NAMES.contains(normalized)) {
            throw BusinessException.badRequest("OPTION_ICON_INVALID", "图标名不在允许范围内");
        }
    }

    private void ensureValidCode(String code) {
        if (!code.matches("^[a-z0-9_]+$")) {
            throw BusinessException.badRequest("OPTION_ITEM_CODE_INVALID", "配置项编码仅支持小写字母、数字和下划线");
        }
    }

    private PublicSystemOptionItemVo toPublicItemVo(SystemOptionItem item) {
        return PublicSystemOptionItemVo.builder()
                .code(item.getItemCode())
                .label(item.getItemLabel())
                .subLabel(item.getItemSubLabel())
                .description(item.getDescription())
                .icon(item.getIconName())
                .sort(item.getSortOrder())
                .enabled(item.getEnabled())
                .extra(parseExtraJson(item.getExtraJson()))
                .build();
    }

    private AdminSystemOptionItemVo toAdminItemVo(SystemOptionItem item) {
        return AdminSystemOptionItemVo.builder()
                .id(item.getId())
                .code(item.getItemCode())
                .label(item.getItemLabel())
                .subLabel(item.getItemSubLabel())
                .description(item.getDescription())
                .icon(item.getIconName())
                .sort(item.getSortOrder())
                .enabled(item.getEnabled())
                .builtin(item.getBuiltin())
                .extra(parseExtraJson(item.getExtraJson()))
                .build();
    }

    private Map<String, Object> parseExtraJson(String extraJson) {
        if (!StringUtils.hasText(extraJson)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(extraJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw BusinessException.badRequest("OPTION_EXTRA_INVALID", "配置项扩展字段格式不合法");
        }
    }

    private String writeExtraJson(Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(extra.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && StringUtils.hasText(String.valueOf(entry.getValue())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new)));
        } catch (JsonProcessingException exception) {
            throw BusinessException.badRequest("OPTION_EXTRA_INVALID", "配置项扩展字段格式不合法");
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw BusinessException.badRequest("OPTION_FIELD_REQUIRED", message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Map<String, Object> buildSnapshot(String groupCode, SystemOptionItem item) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("groupCode", groupCode);
        snapshot.put("itemCode", item.getItemCode());
        snapshot.put("label", item.getItemLabel());
        snapshot.put("subLabel", item.getItemSubLabel());
        snapshot.put("description", item.getDescription());
        snapshot.put("iconName", item.getIconName());
        snapshot.put("sortOrder", item.getSortOrder());
        snapshot.put("enabled", item.getEnabled());
        return snapshot;
    }
}
