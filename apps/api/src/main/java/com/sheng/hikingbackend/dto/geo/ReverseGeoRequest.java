package com.sheng.hikingbackend.dto.geo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseGeoRequest {

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度不能小于 -180")
    @DecimalMax(value = "180.0", message = "经度不能大于 180")
    private BigDecimal lng;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度不能小于 -90")
    @DecimalMax(value = "90.0", message = "纬度不能大于 90")
    private BigDecimal lat;
}
