package com.sheng.hikingbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.exception.GlobalExceptionHandler;
import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.service.LandscapePredictionService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.TrailWeatherService;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;

@WebMvcTest(TrailController.class)
@Import(GlobalExceptionHandler.class)
class TrailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrailService trailService;

    @MockitoBean
    private TrailWeatherService trailWeatherService;

    @MockitoBean
    private LandscapePredictionService landscapePredictionService;

    @Test
    void createTrailShouldBindStructuredGeoFieldsAndReturnSuccess() throws Exception {
        TrailDetailVo response = buildTrailDetail();
        when(trailService.createTrail(eq(2001L), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/trails")
                .with(authentication(buildAuthentication()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTrailBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("路线发布成功"))
                .andExpect(jsonPath("$.data.name").value("武功山反穿"))
                .andExpect(jsonPath("$.data.geoProvince").value("江西"))
                .andExpect(jsonPath("$.data.geoCity").value("萍乡"))
                .andExpect(jsonPath("$.data.geoDistrict").value("芦溪"));

        verify(trailService).createTrail(eq(2001L), any(), any());
    }

    @Test
    void createTrailShouldRejectMissingLocation() throws Exception {
        CreateTrailBody body = new CreateTrailBody();
        body.location = "";

        mockMvc.perform(post("/api/trails")
                .with(authentication(buildAuthentication()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("location: 所在位置不能为空")));
    }

    @Test
    void updateTrailShouldBindTrackAndGeoFields() throws Exception {
        TrailDetailVo response = buildTrailDetail();
        when(trailService.updateTrail(eq(3001L), eq(2001L), any(), any())).thenReturn(response);

        CreateTrailBody body = new CreateTrailBody();
        body.trackMediaId = 6002L;
        body.geoSource = "track_reverse";

        mockMvc.perform(put("/api/trails/3001")
                .with(authentication(buildAuthentication()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("路线更新成功"));

        verify(trailService).updateTrail(eq(3001L), eq(2001L), any(), any());
    }

    private UsernamePasswordAuthenticationToken buildAuthentication() {
        User user = new User();
        user.setId(2001L);
        user.setEmail("tester@example.com");
        user.setUsername("tester");
        user.setPasswordHash("password");
        user.setRole(UserRole.USER);
        CustomUserDetails userDetails = CustomUserDetails.from(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private TrailDetailVo buildTrailDetail() {
        return TrailDetailVo.builder()
                .id(3001L)
                .image("https://img.example.com/cover.png")
                .name("武功山反穿")
                .location("萍乡 芦溪")
                .geoCountry("中国")
                .geoProvince("江西")
                .geoCity("萍乡")
                .geoDistrict("芦溪")
                .geoSource("track_reverse")
                .difficulty("moderate")
                .difficultyLabel("适中")
                .packType("light")
                .durationType("single_day")
                .rating(BigDecimal.ZERO)
                .reviewCount(0)
                .distance("16.4 km")
                .elevation("+1128 m")
                .duration("5h 8m")
                .description("适合周末的一条山脊线路")
                .tags(List.of("云海", "单日"))
                .favorites(0)
                .likes(0)
                .likedByCurrentUser(false)
                .favoritedByCurrentUser(false)
                .authorId(2001L)
                .publishTime("刚刚")
                .createdAt(LocalDateTime.now())
                .ownedByCurrentUser(true)
                .editableByCurrentUser(true)
                .author(UserSummaryVo.builder().id(2001L).username("tester").avatar("测").avatarBg("#1f6b3b").build())
                .gallery(List.of())
                .build();
    }

    static class CreateTrailBody {
        public String name = "武功山反穿";
        public String location = "萍乡 芦溪";
        public String geoCountry = "中国";
        public String geoProvince = "江西";
        public String geoCity = "萍乡";
        public String geoDistrict = "芦溪";
        public String geoSource = "track_reverse";
        public String difficulty = "moderate";
        public String difficultyLabel = "适中";
        public String packType = "light";
        public String durationType = "single_day";
        public String distance = "16.4 km";
        public String elevation = "+1128 m";
        public String duration = "5h 8m";
        public String description = "适合周末的一条山脊线路";
        public Long coverMediaId = 5001L;
        public List<String> tags = List.of("云海", "单日");
        public List<Long> galleryMediaIds = List.of();
        public Long trackMediaId = null;
    }
}
