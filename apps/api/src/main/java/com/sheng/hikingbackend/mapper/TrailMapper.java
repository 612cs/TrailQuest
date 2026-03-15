package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.dto.trail.TrailPageRequest;
import com.sheng.hikingbackend.entity.Trail;
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
              <if test="query.keyword != null and query.keyword != ''">
                AND (
                  t.name LIKE CONCAT('%', #{query.keyword}, '%')
                  OR t.location LIKE CONCAT('%', #{query.keyword}, '%')
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
            ORDER BY t.created_at DESC
            </script>
            """)
    IPage<TrailQueryRow> selectTrailPage(Page<TrailQueryRow> page, @Param("query") TrailPageRequest query);

    @Select("""
            SELECT
              t.id,
              t.image,
              t.name,
              t.location,
              t.ip,
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
            LIMIT 1
            """)
    TrailQueryRow selectTrailDetailById(@Param("id") Long id);
}
