package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.Review;
import com.sheng.hikingbackend.vo.review.ReviewQueryRow;
import com.sheng.hikingbackend.vo.review.TrailReviewStatsVo;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("""
            SELECT
              r.id,
              r.trail_id,
              r.user_id,
              r.parent_id,
              r.rating,
              r.time_label,
              r.text,
              r.reply_to,
              r.created_at,
              u.username,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            WHERE r.trail_id = #{trailId}
            ORDER BY r.created_at ASC, r.id ASC
            """)
    List<ReviewQueryRow> selectReviewRowsByTrailId(@Param("trailId") Long trailId);

    @Select("""
            SELECT
              COUNT(*) AS review_count,
              COALESCE(AVG(rating), 0) AS average_rating
            FROM reviews
            WHERE trail_id = #{trailId}
              AND parent_id IS NULL
            """)
    TrailReviewStatsVo selectTrailReviewStats(@Param("trailId") Long trailId);
}
