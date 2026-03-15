package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    Long countByEmail(@Param("email") String email);
}
