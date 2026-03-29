package com.sheng.hikingbackend.dto.trail;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTrailRequest {

    @NotBlank(message = "路线名称不能为空")
    private String name;

    @NotBlank(message = "所在位置不能为空")
    private String location;
    private String geoCountry;
    private String geoProvince;
    private String geoCity;
    private String geoDistrict;
    private String geoSource;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotBlank(message = "难度文案不能为空")
    private String difficultyLabel;

    @NotBlank(message = "负重类型不能为空")
    private String packType;

    @NotBlank(message = "行程类型不能为空")
    private String durationType;

    private String distance;
    private String elevation;
    private String duration;

    private String description;

    @NotNull(message = "封面文件不能为空")
    private Long coverMediaId;

    private List<String> tags;

    private List<Long> galleryMediaIds;

    private Long trackMediaId;
}
