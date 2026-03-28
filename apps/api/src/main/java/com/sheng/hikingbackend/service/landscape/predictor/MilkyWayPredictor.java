package com.sheng.hikingbackend.service.landscape.predictor;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.service.landscape.model.MoonAstronomyPoint;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;
import com.sheng.hikingbackend.vo.landscape.LandscapeMilkyWayPredictionVo;

@Component
public class MilkyWayPredictor {

    public LandscapeMilkyWayPredictionVo predict(LandscapeContext context) {
        if (context.sunForecast().isEmpty() || context.moonForecast().isEmpty()) {
            return LandscapeMilkyWayPredictionVo.builder()
                    .enabled(Boolean.FALSE)
                    .summary("暂时缺少银河观测所需的天文数据")
                    .confidence(0.2)
                    .resolvedFrom(context.resolvedFrom())
                    .visible(Boolean.FALSE)
                    .build();
        }

        MilkyCandidate candidate = context.sunForecast().stream()
                .map(sun -> buildCandidate(context, sun, findMoon(context, sun.date())))
                .max(Comparator.comparingDouble(MilkyCandidate::score))
                .orElse(null);

        if (candidate == null) {
            return LandscapeMilkyWayPredictionVo.builder()
                    .enabled(Boolean.FALSE)
                    .summary("未来几天暂未找到稳定的观测窗口")
                    .confidence(0.3)
                    .resolvedFrom(context.resolvedFrom())
                    .visible(Boolean.FALSE)
                    .build();
        }

        return LandscapeMilkyWayPredictionVo.builder()
                .enabled(Boolean.TRUE)
                .score(candidate.score())
                .level(resolveLevel(candidate.score()))
                .bestWindow(candidate.bestWindow())
                .summary(candidate.score() >= 0.65
                        ? "夜间月光干扰较小，云量条件也相对友好，具备银河观测机会。"
                        : "夜间月光、云量或光污染存在干扰，银河观测条件一般。")
                .risks(buildRisks(context.currentWeather(), context, candidate.score()))
                .confidence(resolveConfidence(context, candidate.score()))
                .resolvedFrom(context.resolvedFrom())
                .visible(candidate.score() >= 0.58)
                .build();
    }

    private MilkyCandidate buildCandidate(LandscapeContext context, SunAstronomyPoint sun, MoonAstronomyPoint moon) {
        if (sun.sunset() == null || sun.sunrise() == null) {
            return new MilkyCandidate(0.0, "夜间时段");
        }
        OffsetDateTime nightStart = sun.sunset().plusHours(2);
        OffsetDateTime nightEnd = sun.sunrise().plusDays(1).minusHours(2);
        if (!nightEnd.isAfter(nightStart)) {
            return new MilkyCandidate(0.0, "夜间时段");
        }

        List<HourlyWeatherPoint> candidates = context.hourlyForecast().stream()
                .filter(point -> !point.fxTime().isBefore(nightStart) && !point.fxTime().isAfter(nightEnd))
                .toList();
        if (candidates.isEmpty()) {
            return new MilkyCandidate(0.0, "夜间时段");
        }

        double bestScore = 0.0;
        HourlyWeatherPoint bestPoint = candidates.get(0);
        for (HourlyWeatherPoint point : candidates) {
            double cloudPenalty = point.cloud() == null ? 0.18 : point.cloud() / 100.0 * 0.42;
            double moonPenalty = moon == null || moon.illumination() == null ? 0.12 : moon.illumination() / 100.0 * 0.28;
            double lightPenalty = context.lightPollution().level() / 5.0 * 0.2;
            double visPenalty = context.currentWeather() != null && context.currentWeather().vis() != null
                    ? Math.max(0.0, 0.18 - Math.min(context.currentWeather().vis().doubleValue() / 25.0, 0.18))
                    : 0.08;
            double altitudeBoost = altitudeBoost(context);
            double windPenalty = point.windSpeed() != null ? Math.min(point.windSpeed().doubleValue() / 20.0, 0.08) : 0.03;
            double score = clamp(1.0 - cloudPenalty - moonPenalty - lightPenalty - visPenalty - windPenalty + altitudeBoost);
            if (score > bestScore) {
                bestScore = score;
                bestPoint = point;
            }
        }

        int start = bestPoint.fxTime().getHour();
        return new MilkyCandidate(bestScore, String.format("%02d:00-%02d:00", start, (start + 2) % 24));
    }

    private MoonAstronomyPoint findMoon(LandscapeContext context, java.time.LocalDate date) {
        return context.moonForecast().stream().filter(item -> item.date().equals(date)).findFirst().orElse(null);
    }

    private double altitudeBoost(LandscapeContext context) {
        double altitude = context.elevationPeakMeters() == null ? 500 : context.elevationPeakMeters().doubleValue();
        return Math.min(altitude / 4000.0, 0.16);
    }

    private List<String> buildRisks(CurrentWeatherSnapshot current, LandscapeContext context, double score) {
        return java.util.stream.Stream.of(
                !context.lightPollution().ready() ? "当前缺少精确光污染栅格，银河结果偏保守" : null,
                current != null && current.vis() != null && current.vis().doubleValue() < 10 ? "当前能见度一般，星空通透度可能受影响" : null,
                score < 0.58 ? "未来几天云量或月光干扰偏强，建议降低预期" : null)
                .filter(item -> item != null && !item.isBlank())
                .toList();
    }

    private double resolveConfidence(LandscapeContext context, double score) {
        double base = context.lightPollution().ready() ? 0.78 : 0.58;
        return clamp((base + score) / 2.0);
    }

    private String resolveLevel(double score) {
        if (score >= 0.85) return "excellent";
        if (score >= 0.70) return "good";
        if (score >= 0.50) return "medium";
        return "poor";
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(0.99, value));
    }

    private record MilkyCandidate(double score, String bestWindow) {
    }
}
