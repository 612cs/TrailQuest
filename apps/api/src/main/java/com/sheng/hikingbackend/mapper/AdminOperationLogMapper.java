package com.sheng.hikingbackend.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheng.hikingbackend.dto.admin.AdminOperationLogPageRequest;
import com.sheng.hikingbackend.entity.AdminOperationLog;
import com.sheng.hikingbackend.vo.admin.AdminOperationLogQueryRow;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {

    @Select("""
            <script>
            SELECT
              l.id,
              l.operator_id,
              l.operator_name,
              l.operator_role,
              l.module_code,
              l.action_code,
              l.target_type,
              l.target_id,
              l.target_title,
              l.reason,
              l.result_status,
              l.before_snapshot,
              l.after_snapshot,
              l.request_id,
              l.ip_address,
              l.user_agent,
              l.created_at
            FROM admin_operation_logs l
            <where>
              <if test="query.moduleCode != null and query.moduleCode != ''">
                AND l.module_code = #{query.moduleCode}
              </if>
              <if test="query.actionCode != null and query.actionCode != ''">
                AND l.action_code = #{query.actionCode}
              </if>
              <if test="query.operatorKeyword != null and query.operatorKeyword != ''">
                AND l.operator_name LIKE CONCAT('%', #{query.operatorKeyword}, '%')
              </if>
              <if test="query.targetType != null and query.targetType != ''">
                AND l.target_type = #{query.targetType}
              </if>
              <if test="query.targetId != null and query.targetId != ''">
                AND CAST(l.target_id AS CHAR) = #{query.targetId}
              </if>
              <if test="query.dateFrom != null and query.dateFrom != ''">
                AND l.created_at &gt;= CONCAT(#{query.dateFrom}, ' 00:00:00')
              </if>
              <if test="query.dateTo != null and query.dateTo != ''">
                AND l.created_at &lt;= CONCAT(#{query.dateTo}, ' 23:59:59')
              </if>
            </where>
            ORDER BY l.created_at DESC, l.id DESC
            </script>
            """)
    IPage<AdminOperationLogQueryRow> selectAdminOperationLogPage(
            Page<AdminOperationLogQueryRow> page,
            @Param("query") AdminOperationLogPageRequest query);

    @Select("""
            SELECT
              l.id,
              l.operator_id,
              l.operator_name,
              l.operator_role,
              l.module_code,
              l.action_code,
              l.target_type,
              l.target_id,
              l.target_title,
              l.reason,
              l.result_status,
              l.before_snapshot,
              l.after_snapshot,
              l.request_id,
              l.ip_address,
              l.user_agent,
              l.created_at
            FROM admin_operation_logs l
            WHERE l.id = #{logId}
            LIMIT 1
            """)
    AdminOperationLogQueryRow selectAdminOperationLogDetailById(@Param("logId") Long logId);
}
