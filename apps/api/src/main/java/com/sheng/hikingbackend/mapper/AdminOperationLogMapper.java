package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.AdminOperationLog;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
