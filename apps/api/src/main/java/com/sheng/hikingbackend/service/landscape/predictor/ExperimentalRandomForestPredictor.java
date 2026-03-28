package com.sheng.hikingbackend.service.landscape.predictor;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import com.sheng.hikingbackend.service.landscape.model.HourlyWeatherPoint;
import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.service.landscape.rf.SimpleRandomForestClassifier;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionCardVo;

public abstract class ExperimentalRandomForestPredictor {

    private final SimpleRandomForestClassifier model;

    protected ExperimentalRandomForestPredictor(Predicate<String> ignored) {
        this.model = trainModel();
    }

    public abstract LandscapePredictionCardVo predict(LandscapeContext context);

    protected abstract List<TrainingSample> trainingSamples();

    protected double predictProbability(double[] features) {
        return model.predictProbability(features);
    }

    protected double averageHumidity(LandscapeContext context) {
        return average(context.hourlyForecast().stream().map(HourlyWeatherPoint::humidity).toList(), 70);
    }

    protected double averageDewGap(LandscapeContext context) {
        return context.hourlyForecast().stream()
                .filter(point -> point.temp() != null && point.dew() != null)
                .mapToDouble(point -> point.temp().doubleValue() - point.dew().doubleValue())
                .average()
                .orElse(4.5);
    }

    protected double morningDewGap(LandscapeContext context) {
        return morningPoints(context).stream()
                .filter(point -> point.temp() != null && point.dew() != null)
                .mapToDouble(point -> point.temp().doubleValue() - point.dew().doubleValue())
                .average()
                .orElse(4.5);
    }

    protected double morningCloudAverage(LandscapeContext context) {
        return average(morningPoints(context).stream().map(HourlyWeatherPoint::cloud).toList(), 45);
    }

    protected double dailyCloudAverage(LandscapeContext context) {
        return average(context.hourlyForecast().stream().map(HourlyWeatherPoint::cloud).toList(), 45);
    }

    protected double morningWindAverage(LandscapeContext context) {
        return averageDecimal(morningPoints(context).stream().map(HourlyWeatherPoint::windSpeed).toList(), 3.5);
    }

    protected double dailyWindAverage(LandscapeContext context) {
        return averageDecimal(context.hourlyForecast().stream().map(HourlyWeatherPoint::windSpeed).toList(), 3.5);
    }

    protected double morningPrecipAverage(LandscapeContext context) {
        return averageDecimal(morningPoints(context).stream().map(HourlyWeatherPoint::precip).toList(), 0.0);
    }

    protected double dailyPrecipAverage(LandscapeContext context) {
        return averageDecimal(context.hourlyForecast().stream().map(HourlyWeatherPoint::precip).toList(), 0.0);
    }

    protected double morningTempAverage(LandscapeContext context) {
        return averageDecimal(morningPoints(context).stream().map(HourlyWeatherPoint::temp).toList(), 8.0);
    }

    protected double dailyTempAverage(LandscapeContext context) {
        return averageDecimal(context.hourlyForecast().stream().map(HourlyWeatherPoint::temp).toList(), 8.0);
    }

    protected double altitude(LandscapeContext context) {
        if (context.elevationPeakMeters() != null) {
            return context.elevationPeakMeters().doubleValue();
        }
        if (context.elevationGainMeters() != null) {
            return Math.max(context.elevationGainMeters().doubleValue() * 1.35, 300);
        }
        return 500;
    }

    protected String resolveBestMorningWindow(LandscapeContext context) {
        List<HourlyWeatherPoint> points = morningPoints(context);
        if (points.isEmpty()) {
            return "清晨时段";
        }
        HourlyWeatherPoint start = points.get(0);
        HourlyWeatherPoint end = points.get(Math.min(points.size() - 1, 2));
        return formatHourRange(start.fxTime().getHour(), end.fxTime().getHour() + 1);
    }

    protected String resolveBestAllDayWindow(LandscapeContext context) {
        if (context.hourlyForecast().isEmpty()) {
            return "全天时段";
        }
        int startHour = context.hourlyForecast().get(0).fxTime().getHour();
        int endHour = context.hourlyForecast().get(Math.min(context.hourlyForecast().size() - 1, 5)).fxTime().getHour();
        return formatHourRange(startHour, endHour + 1);
    }

    protected String resolveLevel(double score) {
        if (score >= 0.85) return "excellent";
        if (score >= 0.70) return "good";
        if (score >= 0.50) return "medium";
        return "poor";
    }

    protected double resolveConfidence(double score, double baseline) {
        return clamp((baseline + score) / 2.0);
    }

    protected double applyRuleAdjustments(double base, double... deltas) {
        double adjusted = base;
        for (double delta : deltas) {
            adjusted += delta;
        }
        return clamp(adjusted);
    }

    protected String resolveSummary(double score, String positive, String negative) {
        return score >= 0.6 ? positive : negative;
    }

    protected List<String> buildRisks(String... risks) {
        return Arrays.stream(risks).filter(item -> item != null && !item.isBlank()).toList();
    }

    protected List<TrainingSample> generateSamples(int size, ToIntFunction<double[]> labelFunction, double[]... ranges) {
        SecureRandom random = new SecureRandom();
        List<TrainingSample> samples = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double[] features = new double[ranges.length];
            for (int j = 0; j < ranges.length; j++) {
                double min = ranges[j][0];
                double max = ranges[j][1];
                features[j] = min + (max - min) * random.nextDouble();
            }
            samples.add(new TrainingSample(features, labelFunction.applyAsInt(features)));
        }
        return samples;
    }

    protected double[] range(double min, double max) {
        return new double[] { min, max };
    }

    protected double clamp(double value) {
        return Math.max(0.0, Math.min(0.99, value));
    }

    private List<HourlyWeatherPoint> morningPoints(LandscapeContext context) {
        return context.hourlyForecast().stream()
                .filter(point -> {
                    int hour = point.fxTime().getHour();
                    return hour >= 4 && hour <= 8;
                })
                .toList();
    }

    private double average(List<Integer> values, double fallback) {
        return values.stream().filter(item -> item != null).mapToInt(Integer::intValue).average().orElse(fallback);
    }

    private double averageDecimal(List<java.math.BigDecimal> values, double fallback) {
        return values.stream().filter(item -> item != null).mapToDouble(java.math.BigDecimal::doubleValue).average().orElse(fallback);
    }

    private String formatHourRange(int startHour, int endHour) {
        return String.format(Locale.CHINA, "%02d:00-%02d:00", startHour % 24, endHour % 24);
    }

    private SimpleRandomForestClassifier trainModel() {
        List<TrainingSample> samples = trainingSamples();
        double[][] features = new double[samples.size()][];
        int[] labels = new int[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            features[i] = samples.get(i).features();
            labels[i] = samples.get(i).label();
        }
        return SimpleRandomForestClassifier.train(features, labels, 25, 4, 2, 4, 42L);
    }

    protected record TrainingSample(double[] features, int label) {
    }
}
