package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrailImageMapper {

    @Insert("""
            INSERT INTO trail_images (trail_id, image, sort_order)
            VALUES (#{trailId}, #{image}, #{sortOrder})
            """)
    int insertImage(@Param("trailId") Long trailId, @Param("image") String image, @Param("sortOrder") int sortOrder);
}
