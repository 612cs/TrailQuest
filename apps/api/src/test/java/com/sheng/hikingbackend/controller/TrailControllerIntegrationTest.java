package com.sheng.hikingbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheng.hikingbackend.common.enums.UserRole;
import com.sheng.hikingbackend.common.exception.GlobalExceptionHandler;
import com.sheng.hikingbackend.config.CustomUserDetails;
import com.sheng.hikingbackend.entity.User;
import com.sheng.hikingbackend.service.LandscapePredictionService;
import com.sheng.hikingbackend.service.TrailService;
import com.sheng.hikingbackend.service.TrailWeatherService;
import com.sheng.hikingbackend.vo.common.UserSummaryVo;
import com.sheng.hikingbackend.vo.trail.TrailDetailVo;

class TrailControllerIntegrationTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TrailService trailService;
    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        trailService = mock(TrailService.class);
        TrailWeatherService trailWeatherService = mock(TrailWeatherService.class);
        LandscapePredictionService landscapePredictionService = mock(LandscapePredictionService.class);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        testUserDetails = (CustomUserDetails) buildAuthentication().getPrincipal();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrailController(trailService, trailWeatherService, landscapePredictionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(CustomUserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory) {
                        return testUserDetails;
                    }
                })
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTrailShouldBindStructuredGeoFieldsAndReturnSuccess() throws Exception {
        TrailDetailVo response = buildTrailDetail();
        when(trailService.createTrail(any(), any(), any())).thenReturn(response);
        SecurityContextHolder.getContext().setAuthentication(buildAuthentication());

        mockMvc.perform(post("/api/trails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateTrailBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("路线已提交审核，请耐心等待"))
                .andExpect(jsonPath("$.data.name").value("武功山反穿"))
                .andExpect(jsonPath("$.data.geoProvince").value("江西"))
                .andExpect(jsonPath("$.data.geoCity").value("萍乡"))
                .andExpect(jsonPath("$.data.geoDistrict").value("芦溪"));

        verify(trailService).createTrail(any(), any(), any());
    }

    @Test
    void createTrailShouldRejectMissingLocation() throws Exception {
        CreateTrailBody body = new CreateTrailBody();
        body.location = "";
        SecurityContextHolder.getContext().setAuthentication(buildAuthentication());

        mockMvc.perform(post("/api/trails")
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
        when(trailService.updateTrail(eq(3001L), any(), any(), any())).thenReturn(response);

        CreateTrailBody body = new CreateTrailBody();
        body.trackMediaId = 6002L;
        body.geoSource = "track_reverse";
        SecurityContextHolder.getContext().setAuthentication(buildAuthentication());

        mockMvc.perform(put("/api/trails/3001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("路线更新已提交审核，请耐心等待"));

        verify(trailService).updateTrail(eq(3001L), any(), any(), any());
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
