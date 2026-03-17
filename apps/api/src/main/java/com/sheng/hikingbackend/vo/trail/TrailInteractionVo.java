package com.sheng.hikingbackend.vo.trail;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrailInteractionVo {

    private Long trailId;
    private Integer likes;
    private Integer favorites;
    private Boolean likedByCurrentUser;
    private Boolean favoritedByCurrentUser;
}
