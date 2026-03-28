package com.sheng.hikingbackend.service.landscape.predictor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sheng.hikingbackend.service.landscape.model.LandscapeContext;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionCardVo;

@Component
public class IcicleRandomForestPredictor extends ExperimentalRandomForestPredictor {

    public IcicleRandomForestPredictor() {
        super(landscapeType -> landscapeType.equals("icicle"));
    }

    @Override
    public LandscapePredictionCardVo predict(LandscapeContext context) {
        double humidity = averageHumidity(context);
        double cloud = dailyCloudAverage(context);
        double wind = dailyWindAverage(context);
        double dewGap = averageDewGap(context);
        double altitude = altitude(context);
        double precip = dailyPrecipAverage(context);
        double temp = dailyTempAverage(context);

        double[] features = new double[] { humidity, cloud, wind, dewGap, altitude, precip, temp };
        double score = predictProbability(features);
        score = applyRuleAdjustments(score,
                temp <= -1 ? 0.18 : -0.18,
                precip >= 0.2 ? 0.08 : -0.02,
                humidity >= 82 ? 0.06 : -0.03,
                altitude >= 800 ? 0.06 : 0.0,
                cloud >= 35 ? 0.03 : 0.0);

        return LandscapePredictionCardVo.builder()
                .enabled(Boolean.TRUE)
                .experimental(Boolean.TRUE)
                .score(score)
                .level(resolveLevel(score))
                .bestWindow(resolveBestAllDayWindow(context))
                .summary(resolveSummary(score,
                        "低温与前期水汽条件具备，冰挂形成概率正在抬升。",
                        "持续低温或补给水汽不足，冰挂条件一般。"))
                .risks(buildRisks(
                        temp > 1 ? "白天气温偏高，冰挂不易持续" : null,
                        precip < 0.1 ? "缺少融水或降水补给，结冰体量可能不足" : null,
                        wind > 10 ? "风速偏大，近崖区域体感风险较高" : null))
                .confidence(resolveConfidence(score, 0.58))
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
            boolean positive = temp <= 0
                    && humidity >= 78
                    && precip >= 0.2
                    && altitude >= 650
                    && wind <= 10
                    && dewGap <= 4;
            return positive ? 1 : 0;
        },
                range(55, 100),
                range(5, 100),
                range(0, 14),
                range(0, 9),
                range(100, 3000),
                range(0, 6),
                range(-15, 10));
    }
}
