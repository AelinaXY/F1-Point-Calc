package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.NSAD;
import org.f1.service.NSADFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private GBTRegressionModel gradientBoostedTreesModel;
    private final NSADFactory nsadFactory;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, NSADFactory nsadFactory) {
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
        this.nsadFactory = nsadFactory;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        NSAD nsad = nsadFactory.createUnlabelled(fullPointEntity, Meeting.getMeeting(raceName), isSprint);
        return gradientBoostedTreesModel.predict(nsad.toFeaturesVector());
    }

    public void reloadModel(){
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
    }
}
