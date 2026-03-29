package com.sheng.hikingbackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.entity.AiConversation;

@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {

    @Select("""
            SELECT c.*
            FROM ai_conversations c
            WHERE c.user_id = #{userId}
              AND c.status = 'active'
            ORDER BY c.updated_at DESC, c.id DESC
            """)
    List<AiConversation> selectActiveByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT *
            FROM ai_conversations
            WHERE id = #{conversationId}
              AND user_id = #{userId}
              AND status = 'active'
            LIMIT 1
            """)
    AiConversation selectOwnedConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
