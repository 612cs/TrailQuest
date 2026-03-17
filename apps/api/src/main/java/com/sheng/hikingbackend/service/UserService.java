package com.sheng.hikingbackend.service;

import com.sheng.hikingbackend.vo.user.UserCardVo;

public interface UserService {

    UserCardVo getUserCard(Long userId);
}
