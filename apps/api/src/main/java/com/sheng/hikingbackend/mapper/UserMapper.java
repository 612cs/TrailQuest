package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheng.hikingbackend.dto.admin.AdminUserPageRequest;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.vo.admin.AdminUserQueryRow;
import com.sheng.hikingbackend.vo.user.UserCardQueryRow;
import com.sheng.hikingbackend.vo.user.UserStatsQueryRow;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    Long countByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users")
    Long countAllUsers();

    @Select("""
            SELECT
              u.id,
              u.username,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url,
              u.role,
              u.location,
              u.bio,
              u.created_at,
              (
                SELECT COUNT(*)
                FROM trails t
                WHERE t.author_id = u.id
                  AND t.status = 'active'
              ) AS post_count,
              (
                SELECT COUNT(*)
                FROM trail_favorites tf
                WHERE tf.user_id = u.id
              ) AS saved_count
            FROM users u
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            WHERE u.id = #{userId}
            LIMIT 1
            """)
    UserCardQueryRow selectUserCardById(@Param("userId") Long userId);

    @Select("""
            SELECT
              (
                SELECT COUNT(*)
                FROM trails t
                WHERE t.author_id = #{userId}
                  AND t.status = 'active'
              ) AS post_count,
            (
                SELECT COUNT(*)
                FROM trail_favorites tf
                WHERE tf.user_id = #{userId}
              ) AS saved_count
            """)
    UserStatsQueryRow selectUserStatsById(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              u.id,
              u.username,
              u.email,
              u.role,
              u.status,
              u.location,
              u.avatar,
              u.avatar_bg,
              mf.url AS avatar_media_url,
              u.banned_at,
              (
                SELECT COUNT(*)
                FROM trails t
                WHERE t.author_id = u.id
                  AND t.status = 'active'
              ) AS published_trail_count,
              u.created_at
            FROM users u
            LEFT JOIN media_files mf ON mf.id = u.avatar_media_id
            <where>
              <if test="query.keyword != null and query.keyword != ''">
                AND (
                  u.username LIKE CONCAT('%', #{query.keyword}, '%')
                  OR u.email LIKE CONCAT('%', #{query.keyword}, '%')
                  OR u.location LIKE CONCAT('%', #{query.keyword}, '%')
                )
              </if>
              <if test="query.role != null and query.role != ''">
                AND u.role = #{query.role}
              </if>
            </where>
            ORDER BY u.created_at DESC
            </script>
            """)
    IPage<AdminUserQueryRow> selectAdminUserPage(Page<AdminUserQueryRow> page, @Param("query") AdminUserPageRequest query);
}
