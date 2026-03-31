package com.sheng.hikingbackend.dto.admin;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateSystemOptionItemRequest {

    @NotBlank(message = "配置项编码不能为空")
    @Size(max = 64, message = "配置项编码长度不能超过 64")
    private String itemCode;

    @NotBlank(message = "配置项名称不能为空")
    @Size(max = 100, message = "配置项名称长度不能超过 100")
    private String itemLabel;

    @Size(max = 100, message = "副文案长度不能超过 100")
    private String itemSubLabel;

    @Size(max = 255, message = "说明长度不能超过 255")
    private String description;

    @Size(max = 64, message = "图标名长度不能超过 64")
    private String iconName;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    private Map<String, Object> extra;
}
