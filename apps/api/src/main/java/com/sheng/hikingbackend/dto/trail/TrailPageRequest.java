package com.sheng.hikingbackend.dto.trail;

import com.sheng.hikingbackend.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrailPageRequest extends PageRequest {

    private String keyword;
    private String difficulty;
    private String packType;
    private String durationType;
    private String distance;
}
