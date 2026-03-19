package com.sheng.hikingbackend.vo.user;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserTrailListItemVo {

    private Long id;
    private String image;
    private String name;
    private String location;
    private Long authorId;
    private String authorUsername;
    private String publishTime;
    private LocalDateTime createdAt;
    private Boolean favoritedByCurrentUser;
    private Boolean likedByCurrentUser;
    private Integer favorites;
    private Integer likes;
}
