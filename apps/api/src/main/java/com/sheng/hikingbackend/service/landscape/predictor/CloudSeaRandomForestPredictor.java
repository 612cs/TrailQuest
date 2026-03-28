package com.sheng.hikingbackend.service.landscape.predictor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionCardVo;

@Component
public class CloudSeaRandomForestPredictor extends ExperimentalRandomForestPredictor {

    public CloudSeaRandomForestPredictor() {
        super(landscapeType -> landscapeType.equals("cloud_sea"));
    }

    @Override
    public LandscapePredictionCardVo predict(LandscapeContext context) {
        double humidity = averageHumidity(context);
        double cloud = morningCloudAverage(context);
        double wind = morningWindAverage(context);
        double dewGap = morningDewGap(context);
        double altitude = altitude(context);
        double precip = morningPrecipAverage(context);
        double temp = morningTempAverage(context);

        double[] features = new double[] { humidity, cloud, wind, dewGap, altitude, precip, temp };
        double score = predictProbability(features);
        score = applyRuleAdjustments(score,
                humidity >= 88 ? 0.12 : -0.08,
                dewGap <= 2.2 ? 0.08 : -0.05,
                wind <= 5.5 ? 0.05 : -0.08,
                cloud >= 25 && cloud <= 80 ? 0.05 : -0.04,
                altitude >= 900 ? 0.08 : 0.0);

        return LandscapePredictionCardVo.builder()
                .enabled(Boolean.TRUE)
                .experimental(Boolean.TRUE)
                .score(score)
                .level(resolveLevel(score))
                .bestWindow(resolveBestMorningWindow(context))
                .summary(resolveSummary(score,
                        "清晨湿度与低温条件较好，具备形成云海的可能。",
                        "湿度或温差条件一般，云海概率偏低。"))
                .risks(buildRisks(
                        wind > 6 ? "高处风力偏大，云层容易被吹散" : null,
                        cloud > 85 ? "厚云偏多，观景通透度可能受影响" : null,
                        precip > 0.5 ? "降水偏明显，登顶视野可能受阻" : null))
                .confidence(resolveConfidence(score, 0.64))
                .resolvedFrom(context.resolvedFrom())
                .build();
    }

    @Override
    protected List<TrainingSample> trainingSamples() {
        return generateSamples(480, sample -> {
            double humidity = sample[0];
            double cloud = sample[1];
            double wind = sample[2];
            double dewGap = sample[3];
            double altitude = sample[4];
            double precip = sample[5];
            double temp = sample[6];
            boolean positive = humidity >= 86
                    && dewGap <= 2.5
                    && wind <= 6.5
                    && cloud >= 20 && cloud <= 82
                    && altitude >= 700
                    && precip <= 1.5
                    && temp >= -2 && temp <= 14;
            return positive ? 1 : 0;
        },
                range(65, 100),
                range(5, 95),
                range(0, 12),
                range(0, 8),
                range(200, 2600),
                range(0, 4),
                range(-5, 18));
    }
}
