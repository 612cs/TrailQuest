package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TagMapper {

    @Select("""
            SELECT id
            FROM tags
            WHERE name = #{name}
            LIMIT 1
            """)
    Long selectIdByName(@Param("name") String name);

    @Insert("""
            INSERT INTO tags (name)
            VALUES (#{name})
            """)
    int insertTag(@Param("name") String name);
}
