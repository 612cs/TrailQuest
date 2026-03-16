package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.ReviewImage;

@Mapper
public interface ReviewImageMapper extends BaseMapper<ReviewImage> {

    @Select("""
            <script>
            SELECT review_id, image
            FROM review_images
            WHERE review_id IN
            <foreach collection="reviewIds" item="reviewId" open="(" separator="," close=")">
              #{reviewId}
            </foreach>
            ORDER BY id ASC
            </script>
            """)
    List<ReviewImage> selectByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @Insert("""
            <script>
            INSERT INTO review_images (review_id, image)
            VALUES
            <foreach collection="images" item="item" separator=",">
              (#{item.reviewId}, #{item.image})
            </foreach>
            </script>
            """)
    int insertBatch(@Param("images") List<ReviewImage> images);
}
