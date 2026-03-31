package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.dto.admin.AdminReviewPageRequest;
import com.sheng.hikingbackend.entity.Review;
import com.sheng.hikingbackend.vo.admin.AdminDashboardDailyCountRow;
import com.sheng.hikingbackend.vo.admin.AdminReviewQueryRow;
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
              AND r.status = 'active'
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
              AND r.status = 'active'
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
              AND status = 'active'
            """)
    TrailReviewStatsVo selectTrailReviewStats(@Param("trailId") Long trailId);

    @Select("""
            <script>
            SELECT
              r.id,
              r.trail_id,
              r.user_id,
              r.parent_id,
              r.rating,
              r.text,
              r.status,
              r.moderation_reason,
              r.moderated_at,
              u.username AS author_username,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url,
              t.name AS trail_name,
              parent.text AS parent_text,
              r.created_at
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            LEFT JOIN trails t ON t.id = r.trail_id
            LEFT JOIN reviews parent ON parent.id = r.parent_id
            <where>
              <if test="query.keyword != null and query.keyword != ''">
                AND r.text LIKE CONCAT('%', #{query.keyword}, '%')
              </if>
              <if test="query.trailKeyword != null and query.trailKeyword != ''">
                AND t.name LIKE CONCAT('%', #{query.trailKeyword}, '%')
              </if>
              <if test="query.authorKeyword != null and query.authorKeyword != ''">
                AND u.username LIKE CONCAT('%', #{query.authorKeyword}, '%')
              </if>
              <if test="query.status != null and query.status != ''">
                AND r.status = #{query.status}
              </if>
            </where>
            ORDER BY r.created_at DESC, r.id DESC
            </script>
            """)
    IPage<AdminReviewQueryRow> selectAdminReviewPage(
            Page<AdminReviewQueryRow> page,
            @Param("query") AdminReviewPageRequest query);

    @Select("""
            SELECT
              r.id,
              r.trail_id,
              r.user_id,
              r.parent_id,
              r.rating,
              r.text,
              r.status,
              r.moderation_reason,
              r.moderated_at,
              u.username AS author_username,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url,
              t.name AS trail_name,
              parent.text AS parent_text,
              r.created_at
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            LEFT JOIN trails t ON t.id = r.trail_id
            LEFT JOIN reviews parent ON parent.id = r.parent_id
            WHERE r.id = #{reviewId}
            """)
    AdminReviewQueryRow selectAdminReviewDetailById(@Param("reviewId") Long reviewId);

    @Select("""
            SELECT
              r.id,
              r.trail_id,
              r.user_id,
              r.parent_id,
              r.rating,
              r.text,
              r.status,
              r.moderation_reason,
              r.moderated_at,
              u.username AS author_username,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url,
              t.name AS trail_name,
              parent.text AS parent_text,
              r.created_at
            FROM reviews r
            JOIN users u ON u.id = r.user_id
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            LEFT JOIN trails t ON t.id = r.trail_id
            LEFT JOIN reviews parent ON parent.id = r.parent_id
            WHERE r.parent_id = #{parentId}
            ORDER BY r.created_at ASC, r.id ASC
            """)
    List<AdminReviewQueryRow> selectAdminReviewRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM reviews")
    long countAllReviews();

    @Select("SELECT COUNT(*) FROM reviews WHERE status = 'hidden'")
    long countHiddenReviews();

    @Select("SELECT COUNT(*) FROM reviews WHERE DATE(created_at) = CURDATE()")
    long countTodayNewReviews();

    @Select("""
            SELECT
              DATE(created_at) AS metric_date,
              COUNT(*) AS metric_count
            FROM reviews
            WHERE created_at >= #{startDateTime}
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) ASC
            """)
    List<AdminDashboardDailyCountRow> selectDailyNewReviewCounts(@Param("startDateTime") java.time.LocalDateTime startDateTime);
}
