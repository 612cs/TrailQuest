package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.dto.admin.AdminTrailManagementPageRequest;
import com.sheng.hikingbackend.dto.admin.AdminTrailPageRequest;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.entity.Trail;
import com.sheng.hikingbackend.vo.trail.TrailInteractionVo;
import com.sheng.hikingbackend.vo.trail.TrailQueryRow;

@Mapper
public interface TrailMapper extends BaseMapper<Trail> {

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.ip,
              t.geo_country,
              t.geo_province,
              t.geo_city,
              t.geo_district,
              t.geo_source,
              t.difficulty,
              t.difficulty_label,
              t.pack_type,
              t.duration_type,
              t.rating,
              t.review_count,
              t.distance,
              t.elevation,
              t.duration,
              t.description,
              t.favorites,
              t.likes,
              <choose>
                <when test="userId != null">
                  EXISTS(
                    SELECT 1
                    FROM trail_likes tl
                    WHERE tl.trail_id = t.id AND tl.user_id = #{userId}
                  ) AS liked_by_current_user,
                  EXISTS(
                    SELECT 1
                    FROM trail_favorites tf
                    WHERE tf.trail_id = t.id AND tf.user_id = #{userId}
                  ) AS favorited_by_current_user,
                </when>
                <otherwise>
                  FALSE AS liked_by_current_user,
                  FALSE AS favorited_by_current_user,
                </otherwise>
              </choose>
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              tag_summary.tags_csv,
              u.username AS author_username,
              u.avatar AS author_avatar,
              u.avatar_bg AS author_avatar_bg
            FROM trails t
            JOIN users u ON u.id = t.author_id
            LEFT JOIN (
              SELECT
                tt.trail_id,
                GROUP_CONCAT(tag.name ORDER BY tag.id SEPARATOR ',') AS tags_csv
              FROM trail_tags tt
              JOIN tags tag ON tag.id = tt.tag_id
              GROUP BY tt.trail_id
            ) tag_summary ON tag_summary.trail_id = t.id
            <where>
              AND t.status = 'active'
              AND t.review_status = 'approved'
              <if test="query.geoDistrict != null and query.geoDistrict != ''">
                AND t.geo_district = #{query.geoDistrict}
              </if>
              <if test="(query.geoDistrict == null or query.geoDistrict == '') and query.geoCity != null and query.geoCity != ''">
                AND t.geo_city = #{query.geoCity}
              </if>
              <if test="(query.geoDistrict == null or query.geoDistrict == '') and (query.geoCity == null or query.geoCity == '') and query.geoProvince != null and query.geoProvince != ''">
                AND t.geo_province = #{query.geoProvince}
              </if>
              <if test="query.keyword != null and query.keyword != ''">
                AND (
                  t.name LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.location LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_province LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_city LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_district LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.description LIKE CONCAT('%', #{query.keyword}, '%')
                )
              </if>
              <if test="query.difficulty != null and query.difficulty != '' and query.difficulty != 'all'">
                AND t.difficulty = #{query.difficulty}
              </if>
              <if test="query.packType != null and query.packType != '' and query.packType != 'all'">
                AND t.pack_type = #{query.packType}
              </if>
              <if test="query.durationType != null and query.durationType != '' and query.durationType != 'all'">
                AND t.duration_type = #{query.durationType}
              </if>
              <choose>
                <when test="query.distance == 'short'">
                  AND CAST(REPLACE(t.distance, ' km', '') AS DECIMAL(10,2)) &lt; 5
                </when>
                <when test="query.distance == 'medium'">
                  AND CAST(REPLACE(t.distance, ' km', '') AS DECIMAL(10,2)) &gt;= 5
                  AND CAST(REPLACE(t.distance, ' km', '') AS DECIMAL(10,2)) &lt;= 10
                </when>
                <when test="query.distance == 'long'">
                  AND CAST(REPLACE(t.distance, ' km', '') AS DECIMAL(10,2)) &gt; 10
                </when>
              </choose>
            </where>
            <choose>
              <when test="query.sort == 'hot'">
                ORDER BY (
                  t.likes * 0.4 +
                  t.favorites * 0.3 +
                  t.review_count * 0.2 -
                  TIMESTAMPDIFF(HOUR, t.created_at, NOW()) * 0.1
                ) DESC, t.created_at DESC
              </when>
              <when test="query.sort == 'rating'">
                ORDER BY t.rating DESC, t.review_count DESC, t.created_at DESC
              </when>
              <otherwise>
                ORDER BY t.created_at DESC
              </otherwise>
            </choose>
            </script>
            """)
    IPage<TrailQueryRow> selectTrailPage(
            Page<TrailQueryRow> page,
            @Param("query") TrailPageRequest query,
            @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.ip,
              t.geo_country,
              t.geo_province,
              t.geo_city,
              t.geo_district,
              t.geo_source,
              t.difficulty,
              t.difficulty_label,
              t.pack_type,
              t.duration_type,
              t.rating,
              t.review_count,
              t.distance,
              t.elevation,
              t.duration,
              t.description,
              t.favorites,
              t.likes,
              <choose>
                <when test="userId != null">
                  EXISTS(
                    SELECT 1
                    FROM trail_likes tl
                    WHERE tl.trail_id = t.id AND tl.user_id = #{userId}
                  ) AS liked_by_current_user,
                  EXISTS(
                    SELECT 1
                    FROM trail_favorites tf
                    WHERE tf.trail_id = t.id AND tf.user_id = #{userId}
                  ) AS favorited_by_current_user,
                </when>
                <otherwise>
                  FALSE AS liked_by_current_user,
                  FALSE AS favorited_by_current_user,
                </otherwise>
              </choose>
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              tag_summary.tags_csv,
              u.username AS author_username,
              u.avatar AS author_avatar,
              u.avatar_bg AS author_avatar_bg
            FROM trails t
            JOIN users u ON u.id = t.author_id
            LEFT JOIN (
              SELECT
                tt.trail_id,
                GROUP_CONCAT(tag.name ORDER BY tag.id SEPARATOR ',') AS tags_csv
              FROM trail_tags tt
              JOIN tags tag ON tag.id = tt.tag_id
              GROUP BY tt.trail_id
            ) tag_summary ON tag_summary.trail_id = t.id
            WHERE t.id = #{id}
              AND t.status = 'active'
              AND (
                t.review_status = 'approved'
                <if test="userId != null">
                  OR t.author_id = #{userId}
                </if>
              )
            LIMIT 1
            </script>
            """)
    TrailQueryRow selectTrailDetailById(@Param("id") Long id, @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              t.id AS trail_id,
              t.likes,
              t.favorites,
              <choose>
                <when test="userId != null">
                  EXISTS(
                    SELECT 1
                    FROM trail_likes tl
                    WHERE tl.trail_id = t.id AND tl.user_id = #{userId}
                  ) AS liked_by_current_user,
                  EXISTS(
                    SELECT 1
                    FROM trail_favorites tf
                    WHERE tf.trail_id = t.id AND tf.user_id = #{userId}
                  ) AS favorited_by_current_user
                </when>
                <otherwise>
                  FALSE AS liked_by_current_user,
                  FALSE AS favorited_by_current_user
                </otherwise>
              </choose>
            FROM trails t
            WHERE t.id = #{trailId}
              AND t.status = 'active'
            LIMIT 1
            </script>
            """)
    TrailInteractionVo selectTrailInteraction(@Param("trailId") Long trailId, @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.favorites,
              t.likes,
              <choose>
                <when test="userId != null">
                  EXISTS(
                    SELECT 1
                    FROM trail_likes tl
                    WHERE tl.trail_id = t.id AND tl.user_id = #{userId}
                  ) AS liked_by_current_user,
                  EXISTS(
                    SELECT 1
                    FROM trail_favorites tf
                    WHERE tf.trail_id = t.id AND tf.user_id = #{userId}
                  ) AS favorited_by_current_user,
                </when>
                <otherwise>
                  FALSE AS liked_by_current_user,
                  FALSE AS favorited_by_current_user,
                </otherwise>
              </choose>
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              u.username AS author_username
            FROM trails t
            JOIN users u ON u.id = t.author_id
            WHERE t.author_id = #{currentUserId}
              AND t.status = 'active'
            ORDER BY t.created_at DESC
            </script>
            """)
    IPage<TrailQueryRow> selectPublishedTrailsByUserId(
            Page<TrailQueryRow> page,
            @Param("currentUserId") Long currentUserId,
            @Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.favorites,
              t.likes,
              <choose>
                <when test="userId != null">
                  EXISTS(
                    SELECT 1
                    FROM trail_likes tl
                    WHERE tl.trail_id = t.id AND tl.user_id = #{userId}
                  ) AS liked_by_current_user,
                  EXISTS(
                    SELECT 1
                    FROM trail_favorites tf
                    WHERE tf.trail_id = t.id AND tf.user_id = #{userId}
                  ) AS favorited_by_current_user,
                </when>
                <otherwise>
                  FALSE AS liked_by_current_user,
                  FALSE AS favorited_by_current_user,
                </otherwise>
              </choose>
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              u.username AS author_username
            FROM trail_favorites favorite
            JOIN trails t ON t.id = favorite.trail_id
            JOIN users u ON u.id = t.author_id
            WHERE favorite.user_id = #{currentUserId}
              AND t.status = 'active'
              AND t.review_status = 'approved'
            ORDER BY favorite.created_at DESC, favorite.id DESC
            </script>
            """)
    IPage<TrailQueryRow> selectFavoriteTrailsByUserId(
            Page<TrailQueryRow> page,
            @Param("currentUserId") Long currentUserId,
            @Param("userId") Long userId);

    @Select("""
            SELECT *
            FROM trails
            WHERE id = #{trailId}
              AND status = 'active'
              AND review_status = 'approved'
            LIMIT 1
            """)
    Trail selectActiveById(@Param("trailId") Long trailId);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.geo_country,
              t.geo_province,
              t.geo_city,
              t.geo_district,
              t.geo_source,
              t.difficulty,
              t.difficulty_label,
              t.pack_type,
              t.duration_type,
              t.rating,
              t.review_count,
              t.distance,
              t.elevation,
              t.duration,
              t.description,
              t.favorites,
              t.likes,
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              tag_summary.tags_csv,
              u.username AS author_username,
              u.avatar AS author_avatar,
              u.avatar_bg AS author_avatar_bg
            FROM trails t
            JOIN users u ON u.id = t.author_id
            LEFT JOIN (
              SELECT
                tt.trail_id,
                GROUP_CONCAT(tag.name ORDER BY tag.id SEPARATOR ',') AS tags_csv
              FROM trail_tags tt
              JOIN tags tag ON tag.id = tt.tag_id
              GROUP BY tt.trail_id
            ) tag_summary ON tag_summary.trail_id = t.id
            <where>
              AND t.status = 'active'
              <if test="query.reviewStatus != null and query.reviewStatus != ''">
                AND t.review_status = #{query.reviewStatus}
              </if>
              <if test="query.keyword != null and query.keyword != ''">
                AND (
                  t.name LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.location LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_province LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_city LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_district LIKE CONCAT('%', #{query.keyword}, '%')
                )
              </if>
              <if test="query.authorKeyword != null and query.authorKeyword != ''">
                AND u.username LIKE CONCAT('%', #{query.authorKeyword}, '%')
              </if>
            </where>
            ORDER BY
              CASE t.review_status
                WHEN 'pending' THEN 0
                WHEN 'rejected' THEN 1
                ELSE 2
              END ASC,
              t.created_at DESC
            </script>
            """)
    IPage<TrailQueryRow> selectAdminTrailPage(Page<TrailQueryRow> page, @Param("query") AdminTrailPageRequest query);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.geo_country,
              t.geo_province,
              t.geo_city,
              t.geo_district,
              t.geo_source,
              t.difficulty,
              t.difficulty_label,
              t.pack_type,
              t.duration_type,
              t.rating,
              t.review_count,
              t.distance,
              t.elevation,
              t.duration,
              t.description,
              t.favorites,
              t.likes,
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              tag_summary.tags_csv,
              u.username AS author_username,
              u.avatar AS author_avatar,
              u.avatar_bg AS author_avatar_bg
            FROM trails t
            JOIN users u ON u.id = t.author_id
            LEFT JOIN (
              SELECT
                tt.trail_id,
                GROUP_CONCAT(tag.name ORDER BY tag.id SEPARATOR ',') AS tags_csv
              FROM trail_tags tt
              JOIN tags tag ON tag.id = tt.tag_id
              GROUP BY tt.trail_id
            ) tag_summary ON tag_summary.trail_id = t.id
            <where>
              AND t.review_status = 'approved'
              <if test="query.status != null and query.status != ''">
                AND t.status = #{query.status}
              </if>
              <if test="query.keyword != null and query.keyword != ''">
                AND (
                  t.name LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.location LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_province LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_city LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.geo_district LIKE CONCAT('%', #{query.keyword}, '%')
                )
              </if>
              <if test="query.authorKeyword != null and query.authorKeyword != ''">
                AND u.username LIKE CONCAT('%', #{query.authorKeyword}, '%')
              </if>
            </where>
            ORDER BY
              CASE t.status
                WHEN 'active' THEN 0
                ELSE 1
              END ASC,
              t.created_at DESC
            </script>
            """)
    IPage<TrailQueryRow> selectAdminTrailManagementPage(
            Page<TrailQueryRow> page,
            @Param("query") AdminTrailManagementPageRequest query);

    @Select("""
            <script>
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.geo_country,
              t.geo_province,
              t.geo_city,
              t.geo_district,
              t.geo_source,
              t.difficulty,
              t.difficulty_label,
              t.pack_type,
              t.duration_type,
              t.rating,
              t.review_count,
              t.distance,
              t.elevation,
              t.duration,
              t.description,
              t.favorites,
              t.likes,
              t.author_id,
              t.created_at,
              t.status,
              t.review_status,
              t.review_remark,
              t.reviewed_by,
              t.reviewed_at,
              tag_summary.tags_csv,
              u.username AS author_username,
              u.avatar AS author_avatar,
              u.avatar_bg AS author_avatar_bg
            FROM trails t
            JOIN users u ON u.id = t.author_id
            LEFT JOIN (
              SELECT
                tt.trail_id,
                GROUP_CONCAT(tag.name ORDER BY tag.id SEPARATOR ',') AS tags_csv
              FROM trail_tags tt
              JOIN tags tag ON tag.id = tt.tag_id
              GROUP BY tt.trail_id
            ) tag_summary ON tag_summary.trail_id = t.id
            WHERE t.id = #{trailId}
            LIMIT 1
            </script>
            """)
    TrailQueryRow selectAdminTrailDetailById(@Param("trailId") Long trailId);

    @Select("""
            SELECT COUNT(*)
            FROM trails
            WHERE status = 'active'
              AND review_status = 'pending'
            """)
    long countPendingReviewTrails();

    @Update("""
            UPDATE trails
            SET likes = likes + 1
            WHERE id = #{trailId}
            """)
    int incrementLikes(@Param("trailId") Long trailId);

    @Update("""
            UPDATE trails
            SET likes = GREATEST(likes - 1, 0)
            WHERE id = #{trailId}
            """)
    int decrementLikes(@Param("trailId") Long trailId);

    @Update("""
            UPDATE trails
            SET favorites = favorites + 1
            WHERE id = #{trailId}
            """)
    int incrementFavorites(@Param("trailId") Long trailId);

    @Update("""
            UPDATE trails
            SET favorites = GREATEST(favorites - 1, 0)
            WHERE id = #{trailId}
            """)
    int decrementFavorites(@Param("trailId") Long trailId);
}
