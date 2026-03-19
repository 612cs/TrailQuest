package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.UserHikingProfile;

@Mapper
public interface UserHikingProfileMapper extends BaseMapper<UserHikingProfile> {

    @Select("SELECT * FROM user_hiking_profiles WHERE user_id = #{userId} LIMIT 1")
    UserHikingProfile selectByUserId(@Param("userId") Long userId);
}
