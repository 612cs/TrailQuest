package com.sheng.hikingbackend.service.landscape.model;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record BigDecimalHolder(BigDecimal value) {
}
