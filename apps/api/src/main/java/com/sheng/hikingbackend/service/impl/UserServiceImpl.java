package com.sheng.hikingbackend.service.impl;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.sheng.hikingbackend.common.exception.BusinessException;
import com.sheng.hikingbackend.mapper.UserMapper;
import com.sheng.hikingbackend.service.UserService;
import com.sheng.hikingbackend.vo.user.UserCardQueryRow;
import com.sheng.hikingbackend.vo.user.UserCardVo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final DateTimeFormatter JOIN_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月加入");

    private final UserMapper userMapper;

    @Override
    public UserCardVo getUserCard(Long userId) {
        UserCardQueryRow row = userMapper.selectUserCardById(userId);
        if (row == null) {
            throw BusinessException.notFound("USER_NOT_FOUND", "用户不存在");
        }

        return UserCardVo.builder()
                .id(row.getId())
                .username(row.getUsername())
                .avatar(row.getAvatar())
                .avatarBg(row.getAvatarBg())
                .avatarMediaUrl(row.getAvatarMediaUrl())
                .role(row.getRole())
                .joinDate(row.getCreatedAt() == null ? "" : row.getCreatedAt().format(JOIN_DATE_FORMATTER))
                .postCount(row.getPostCount() == null ? 0 : row.getPostCount())
                .savedCount(row.getSavedCount() == null ? 0 : row.getSavedCount())
                .location("未知地区")
                .bio("此用户没有填写个人简介")
                .build();
    }
}
