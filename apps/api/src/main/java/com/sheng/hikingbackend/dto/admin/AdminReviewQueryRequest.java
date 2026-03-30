package com.sheng.hikingbackend.dto.admin;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewQueryRequest extends PageRequest {

    private String keyword;
}
