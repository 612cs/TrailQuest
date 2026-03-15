package com.sheng.hikingbackend.config;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.entity.User;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String displayName;
    private final String password;
    private final UserRole role;
    private final List<GrantedAuthority> authorities;

    private CustomUserDetails(
            Long id,
            String email,
            String displayName,
            String password,
            UserRole role,
            List<GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
    }

    public static CustomUserDetails from(User user) {
        UserRole role = user.getRole() == null ? UserRole.USER : user.getRole();
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPasswordHash(),
                role,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
