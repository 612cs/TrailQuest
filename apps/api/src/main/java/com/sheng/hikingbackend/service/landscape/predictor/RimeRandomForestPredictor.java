package com.sheng.hikingbackend.service.landscape.predictor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionCardVo;

@Component
public class RimeRandomForestPredictor extends ExperimentalRandomForestPredictor {

    public RimeRandomForestPredictor() {
        super(landscapeType -> landscapeType.equals("rime"));
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
                temp <= 0 ? 0.18 : -0.2,
                humidity >= 90 ? 0.1 : -0.06,
                wind >= 2 && wind <= 8 ? 0.05 : -0.04,
                altitude >= 1000 ? 0.08 : 0.0,
                dewGap <= 1.8 ? 0.08 : -0.05);

        return LandscapePredictionCardVo.builder()
                .enabled(Boolean.TRUE)
                .experimental(Boolean.TRUE)
                .score(score)
                .level(resolveLevel(score))
                .bestWindow(resolveBestMorningWindow(context))
                .summary(resolveSummary(score,
                        "低温高湿叠加清晨风场，具备雾凇形成条件。",
                        "温度或湿度不足，雾凇形成概率较低。"))
                .risks(buildRisks(
                        temp > 0 ? "气温高于冰点，雾凇较难稳定形成" : null,
                        wind > 9 ? "风速偏大，附着结晶不易维持" : null,
                        precip > 1 ? "降水偏多，雾凇观感可能被削弱" : null))
                .confidence(resolveConfidence(score, 0.6))
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
            boolean positive = temp <= 0.5
                    && humidity >= 88
                    && dewGap <= 2
                    && wind >= 1.5 && wind <= 8.5
                    && altitude >= 850
                    && cloud >= 15
                    && precip <= 2.0;
            return positive ? 1 : 0;
        },
                range(65, 100),
                range(5, 95),
                range(0, 12),
                range(0, 8),
                range(200, 2800),
                range(0, 4),
                range(-12, 12));
    }
}
