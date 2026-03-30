package com.sheng.hikingbackend.config;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sheng.hikingbackend.common.enums.UserStatus;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (user.getStatus() != null && !UserStatus.ACTIVE.getCode().equals(user.getStatus())) {
            throw new DisabledException("账号不可用");
        }
        return CustomUserDetails.from(user);
    }
}
