package com.sheng.hikingbackend.service.impl;

import org.springframework.stereotype.Service;

import com.sheng.hikingbackend.service.LandscapeContextService;
import com.sheng.hikingbackend.service.LandscapePredictionService;
import com.sheng.hikingbackend.service.landscape.predictor.CloudSeaRandomForestPredictor;
import com.sheng.hikingbackend.service.landscape.predictor.IcicleRandomForestPredictor;
import com.sheng.hikingbackend.service.landscape.predictor.MilkyWayPredictor;
import com.sheng.hikingbackend.service.landscape.predictor.RimeRandomForestPredictor;
import com.sheng.hikingbackend.service.landscape.predictor.SunriseSunsetPredictor;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionResponseVo;
import com.sheng.hikingbackend.vo.landscape.LandscapePredictionSourceVo;

@Service
public class LandscapePredictionServiceImpl implements LandscapePredictionService {

    private final LandscapeContextService landscapeContextService;
    private final SunriseSunsetPredictor sunriseSunsetPredictor;
    private final MilkyWayPredictor milkyWayPredictor;
    private final CloudSeaRandomForestPredictor cloudSeaPredictor;
    private final RimeRandomForestPredictor rimePredictor;
    private final IcicleRandomForestPredictor iciclePredictor;

    public LandscapePredictionServiceImpl(
            LandscapeContextService landscapeContextService,
            SunriseSunsetPredictor sunriseSunsetPredictor,
            MilkyWayPredictor milkyWayPredictor,
            CloudSeaRandomForestPredictor cloudSeaPredictor,
            RimeRandomForestPredictor rimePredictor,
            IcicleRandomForestPredictor iciclePredictor) {
        this.landscapeContextService = landscapeContextService;
        this.sunriseSunsetPredictor = sunriseSunsetPredictor;
        this.milkyWayPredictor = milkyWayPredictor;
        this.cloudSeaPredictor = cloudSeaPredictor;
        this.rimePredictor = rimePredictor;
        this.iciclePredictor = iciclePredictor;
    }

    @Override
    public LandscapePredictionResponseVo getTrailPrediction(Long trailId, int days) {
        int normalizedDays = Math.min(Math.max(days, 1), 7);
        var context = landscapeContextService.build(trailId, normalizedDays);
        return LandscapePredictionResponseVo.builder()
                .sunriseSunset(sunriseSunsetPredictor.predict(context))
                .milkyWay(milkyWayPredictor.predict(context))
                .cloudSea(cloudSeaPredictor.predict(context))
                .rime(rimePredictor.predict(context))
                .icicle(iciclePredictor.predict(context))
                .source(LandscapePredictionSourceVo.builder()
                        .provider("qweather")
                        .weatherReady(context.weatherReady())
                        .astroReady(context.astroReady())
                        .lightPollutionReady(context.lightPollution().ready())
                        .build())
                .build();
    }
}
