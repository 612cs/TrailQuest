package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.AppSetting;

@Mapper
public interface AppSettingMapper extends BaseMapper<AppSetting> {

    @Select("""
            SELECT *
            FROM app_settings
            WHERE setting_key = #{settingKey}
            LIMIT 1
            """)
    AppSetting selectByKey(@Param("settingKey") String settingKey);

    @Delete("""
            DELETE FROM app_settings
            WHERE setting_key = #{settingKey}
            """)
    int deleteByKey(@Param("settingKey") String settingKey);
}
