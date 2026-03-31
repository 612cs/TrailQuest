package com.sheng.hikingbackend.service;

import java.util.List;
import java.util.Map;

import com.sheng.hikingbackend.dto.admin.AdminCreateSystemOptionItemRequest;
import com.sheng.hikingbackend.dto.admin.AdminUpdateSystemOptionItemRequest;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionGroupVo;
import com.sheng.hikingbackend.vo.config.AdminSystemOptionItemVo;
import com.sheng.hikingbackend.vo.config.PublicSystemOptionItemVo;

public interface OptionConfigService {

    Map<String, List<PublicSystemOptionItemVo>> getPublicOptions(List<String> groupCodes);

    List<AdminSystemOptionGroupVo> getAdminGroups();

    List<AdminSystemOptionItemVo> getAdminGroupItems(String groupCode);

    AdminSystemOptionItemVo createAdminGroupItem(String groupCode, Long adminUserId, AdminCreateSystemOptionItemRequest request);

    AdminSystemOptionItemVo updateAdminGroupItem(String groupCode, Long itemId, Long adminUserId, AdminUpdateSystemOptionItemRequest request);
}
