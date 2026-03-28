package com.sheng.hikingbackend.service.landscape.predictor;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.landscape.model.CurrentWeatherSnapshot;
import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.service.landscape.model.SunAstronomyPoint;
import com.sheng.hikingbackend.vo.landscape.LandscapeSunriseSunsetPredictionVo;

@Component
public class SunriseSunsetPredictor {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public LandscapeSunriseSunsetPredictionVo predict(LandscapeContext context) {
        if (context.sunForecast().isEmpty()) {
            return LandscapeSunriseSunsetPredictionVo.builder()
                    .enabled(Boolean.FALSE)
                    .summary("暂时缺少日出日落天文数据")
                    .confidence(0.2)
                    .resolvedFrom(context.resolvedFrom())
                    .build();
        }

        SunCandidate sunriseCandidate = context.sunForecast().stream()
                .map(point -> buildCandidate(point, context, true))
                .max(Comparator.comparingDouble(SunCandidate::score))
                .orElse(null);
        SunCandidate sunsetCandidate = context.sunForecast().stream()
                .map(point -> buildCandidate(point, context, false))
                .max(Comparator.comparingDouble(SunCandidate::score))
                .orElse(null);

        double sunriseScore = sunriseCandidate == null ? 0.0 : sunriseCandidate.score();
        double sunsetScore = sunsetCandidate == null ? 0.0 : sunsetCandidate.score();
        double overallScore = Math.max(sunriseScore, sunsetScore);
        String bestWindow = sunriseScore >= sunsetScore
                ? (sunriseCandidate == null ? "清晨时段" : sunriseCandidate.bestWindow())
                : (sunsetCandidate == null ? "傍晚时段" : sunsetCandidate.bestWindow());

        return LandscapeSunriseSunsetPredictionVo.builder()
                .enabled(Boolean.TRUE)
                .score(overallScore)
                .level(resolveLevel(overallScore))
                .bestWindow(bestWindow)
                .summary(buildSummary(sunriseScore, sunsetScore))
                .risks(buildRisks(context.currentWeather(), sunriseCandidate, sunsetCandidate))
                .confidence(resolveConfidence(context.currentWeather(), overallScore))
                .resolvedFrom(context.resolvedFrom())
                .sunriseTime(formatTime(context.sunForecast().get(0).sunrise()))
                .sunsetTime(formatTime(context.sunForecast().get(0).sunset()))
                .sunriseScore(sunriseScore)
                .sunsetScore(sunsetScore)
                .build();
    }

    private SunCandidate buildCandidate(SunAstronomyPoint point, LandscapeContext context, boolean sunrise) {
        OffsetDateTime targetTime = sunrise ? point.sunrise() : point.sunset();
        if (targetTime == null) {
            return new SunCandidate(0.0, sunrise ? "清晨时段" : "傍晚时段");
        }
        HourlyWeatherPoint hourly = nearestHourly(context.hourlyForecast(), targetTime);
        CurrentWeatherSnapshot current = context.currentWeather();

        double cloudPenalty = hourly != null && hourly.cloud() != null ? hourly.cloud() / 100.0 * 0.45 : 0.18;
        double popPenalty = hourly != null && hourly.pop() != null ? hourly.pop() / 100.0 * 0.2 : 0.06;
        double windPenalty = hourly != null && hourly.windSpeed() != null
                ? Math.min(hourly.windSpeed().doubleValue() / 18.0, 0.16)
                : 0.05;
        double visPenalty = current != null && current.vis() != null
                ? Math.max(0.0, 0.18 - Math.min(current.vis().doubleValue() / 25.0, 0.18))
                : 0.08;
        double rainPenalty = hourly != null && hourly.precip() != null
                ? Math.min(hourly.precip().doubleValue() / 6.0, 0.12)
                : 0.0;
        double solarBoost = sunrise
                ? point.sunriseSolarElevation() != null && point.sunriseSolarElevation().value() != null ? 0.05 : 0.0
                : point.sunsetSolarElevation() != null && point.sunsetSolarElevation().value() != null ? 0.05 : 0.0;

        double score = clamp(1.0 - cloudPenalty - popPenalty - windPenalty - visPenalty - rainPenalty + solarBoost);
        int startHour = sunrise ? Math.max(targetTime.getHour() - 1, 0) : Math.max(targetTime.getHour() - 1, 0);
        int endHour = sunrise ? Math.min(targetTime.getHour() + 1, 23) : Math.min(targetTime.getHour() + 1, 23);
        return new SunCandidate(score, String.format("%02d:00-%02d:00", startHour, endHour));
    }

    private HourlyWeatherPoint nearestHourly(List<HourlyWeatherPoint> points, OffsetDateTime targetTime) {
        return points.stream()
                .min(Comparator.comparingLong(point -> Math.abs(point.fxTime().toEpochSecond() - targetTime.toEpochSecond())))
                .orElse(null);
    }

    private String buildSummary(double sunriseScore, double sunsetScore) {
        if (sunriseScore >= 0.7 && sunsetScore >= 0.7) {
            return "未来几天清晨和傍晚的通透度都不错，适合安排观景与拍摄。";
        }
        if (sunriseScore >= sunsetScore) {
            return sunriseScore >= 0.6 ? "清晨云量较少，日出可见度更值得期待。" : "清晨云量或能见度一般，日出成功率偏低。";
        }
        return sunsetScore >= 0.6 ? "傍晚条件更稳定，日落时段更适合守候。" : "傍晚云层偏厚，日落可见度一般。";
    }

    private List<String> buildRisks(CurrentWeatherSnapshot current, SunCandidate sunrise, SunCandidate sunset) {
        return java.util.stream.Stream.of(
                current != null && current.vis() != null && current.vis().doubleValue() < 8 ? "当前能见度一般，远景层次可能受影响" : null,
                current != null && current.windSpeed() != null && current.windSpeed().doubleValue() > 8 ? "山顶风速偏大，体感温度会更低" : null,
                Math.max(sunrise.score(), sunset.score()) < 0.55 ? "未来几天云层条件不算理想，建议降低预期" : null)
                .filter(item -> item != null && !item.isBlank())
                .toList();
    }

    private double resolveConfidence(CurrentWeatherSnapshot current, double score) {
        double base = current != null && current.vis() != null ? 0.78 : 0.66;
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

    private String formatTime(OffsetDateTime time) {
        return time == null ? null : TIME_FORMATTER.format(time);
    }

    private record SunCandidate(double score, String bestWindow) {
    }
}
