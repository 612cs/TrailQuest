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
            <script>
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
              AND r.parent_id IS NULL
              <if test="cursor != null">
                AND (
                  r.created_at &lt; (SELECT created_at FROM reviews WHERE id = #{cursor})
                  OR (
                    r.created_at = (SELECT created_at FROM reviews WHERE id = #{cursor})
                    AND r.id &lt; #{cursor}
                  )
                )
              </if>
            ORDER BY r.created_at DESC, r.id DESC
            LIMIT #{limit}
            </script>
            """)
    List<ReviewQueryRow> selectTopLevelReviewRowsByTrailId(
            @Param("trailId") Long trailId,
            @Param("cursor") Long cursor,
            @Param("limit") Integer limit);

    @Select("""
            <script>
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
              AND (
                r.parent_id IN
                <foreach collection="rootIds" item="rootId" open="(" separator="," close=")">
                  #{rootId}
                </foreach>
                OR r.parent_id IN (
                  SELECT level1.id
                  FROM reviews level1
                  WHERE level1.parent_id IN
                  <foreach collection="rootIds" item="rootId" open="(" separator="," close=")">
                    #{rootId}
                  </foreach>
                )
              )
            ORDER BY r.created_at ASC, r.id ASC
            </script>
            """)
    List<ReviewQueryRow> selectRepliesByRootIds(
            @Param("trailId") Long trailId,
            @Param("rootIds") List<Long> rootIds);

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
