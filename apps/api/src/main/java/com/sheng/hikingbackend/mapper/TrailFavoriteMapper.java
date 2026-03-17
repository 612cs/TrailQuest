package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.TrailFavorite;

@Mapper
public interface TrailFavoriteMapper extends BaseMapper<TrailFavorite> {

    @Select("""
            SELECT COUNT(1)
            FROM trail_favorites
            WHERE trail_id = #{trailId} AND user_id = #{userId}
            """)
    int countByTrailIdAndUserId(@Param("trailId") Long trailId, @Param("userId") Long userId);

    @Insert("""
            INSERT INTO trail_favorites (trail_id, user_id)
            VALUES (#{trailId}, #{userId})
            """)
    int insertRelation(@Param("trailId") Long trailId, @Param("userId") Long userId);

    @Delete("""
            DELETE FROM trail_favorites
            WHERE trail_id = #{trailId} AND user_id = #{userId}
            """)
    int deleteRelation(@Param("trailId") Long trailId, @Param("userId") Long userId);
}
