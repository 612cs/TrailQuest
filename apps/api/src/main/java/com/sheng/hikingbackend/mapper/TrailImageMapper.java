package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import lombok.Getter;
import lombok.Setter;

@Mapper
public interface TrailImageMapper {

    @Insert("""
            INSERT INTO trail_images (trail_id, image, sort_order)
            VALUES (#{trailId}, #{image}, #{sortOrder})
            """)
    int insertImage(@Param("trailId") Long trailId, @Param("image") String image, @Param("sortOrder") int sortOrder);

    @Delete("""
            DELETE FROM trail_images
            WHERE trail_id = #{trailId}
            """)
    int deleteByTrailId(@Param("trailId") Long trailId);

    @Select("""
            SELECT image, sort_order
            FROM trail_images
            WHERE trail_id = #{trailId}
            ORDER BY sort_order ASC, id ASC
            """)
    List<TrailImageRow> selectByTrailId(@Param("trailId") Long trailId);

    @Getter
    @Setter
    class TrailImageRow {
        private String image;
        private Integer sortOrder;
    }
}
