package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.TrailTrack;

@Mapper
public interface TrailTrackMapper extends BaseMapper<TrailTrack> {

    @Select("""
            SELECT *
            FROM trail_tracks
            WHERE trail_id = #{trailId}
            LIMIT 1
            """)
    TrailTrack selectByTrailId(@Param("trailId") Long trailId);
}
