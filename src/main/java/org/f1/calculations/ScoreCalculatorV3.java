package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.domain.FullPointEntity;
import org.f1.domain.NSAD;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final JavaSparkContext sparkContext;
    private final GradientBoostedTreesModel gradientBoostedTreesModel;

    public ScoreCalculatorV3(JavaSparkContext javaSparkContext) {
        this.sparkContext = javaSparkContext;

        this.gradientBoostedTreesModel = GradientBoostedTreesModel
                .load(sparkContext.sc(), "src/main/resources/regressionModelV2");
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        NSAD nsad = NSAD.buildBaseNSAD(fullPointEntity, raceName);

        double output = gradientBoostedTreesModel.predict(nsad.toLabeledPoint().features());

        if (isSprint) {
            output = output * 1.19;
        }
        return output;
    }


}
