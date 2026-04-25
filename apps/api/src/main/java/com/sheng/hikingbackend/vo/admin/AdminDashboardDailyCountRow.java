package com.sheng.hikingbackend.vo.admin;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardDailyCountRow {

    private LocalDate metricDate;
    private long metricCount;
}
