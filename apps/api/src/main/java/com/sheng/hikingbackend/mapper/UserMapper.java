package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.vo.user.UserCardQueryRow;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE email = #{email} LIMIT 1")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    Long countByEmail(@Param("email") String email);

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
}
