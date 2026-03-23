package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrailTagMapper {

    @Insert("""
            INSERT INTO trail_tags (trail_id, tag_id)
            VALUES (#{trailId}, #{tagId})
            """)
    int insertRelation(@Param("trailId") Long trailId, @Param("tagId") Long tagId);

    @Delete("""
            DELETE FROM trail_tags
            WHERE trail_id = #{trailId}
            """)
    int deleteByTrailId(@Param("trailId") Long trailId);
}
