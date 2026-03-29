package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.AiMessage;

@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {

    @Select("""
            SELECT *
            FROM ai_messages
            WHERE conversation_id = #{conversationId}
            ORDER BY created_at ASC, id ASC
            """)
    List<AiMessage> selectByConversationId(@Param("conversationId") Long conversationId);

    @Select("""
            SELECT *
            FROM ai_messages
            WHERE conversation_id = #{conversationId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<AiMessage> selectLatestByConversationId(@Param("conversationId") Long conversationId, @Param("limit") int limit);

    @Select("""
            SELECT content
            FROM ai_messages
            WHERE conversation_id = #{conversationId}
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    String selectLatestContent(@Param("conversationId") Long conversationId);
}
