package com.sheng.hikingbackend.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheng.hikingbackend.common.ApiResponse;

@RestController
@RequestMapping("/test")
public class DbTestController {

    private final JdbcTemplate jdbcTemplate;

    public DbTestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public ApiResponse<Map<String, Object>> testDatabaseConnection() {
        Integer ping = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "database connected");
        result.put("ping", ping);
        return ApiResponse.success(result);
    }

    @GetMapping("/users-count")
    public ApiResponse<Map<String, Object>> testUsersCount() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("table", "users");
        result.put("count", count);
        return ApiResponse.success(result);
    }
}
