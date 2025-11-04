package org.f1.calculations;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.domain.FullPointEntity;
import org.f1.domain.NSAD;
import org.f1.domain.Race;
import org.f1.utils.MathUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final JavaSparkContext sparkContext;

    private final GradientBoostedTreesModel gradientBoostedTreesModel;

    public ScoreCalculatorV3(JavaSparkContext javaSparkContext) {

        this.sparkContext = javaSparkContext;

        this.gradientBoostedTreesModel = GradientBoostedTreesModel
                .load(sparkContext.sc(), "src/main/resources/regressionModel");
    }

    @Override
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {

        List<Double> pointList = getListOfPoints(fullPointEntity.getRaceList(), raceName);
        Double avgPoints = ScoreCalculator.calcAveragePoints(pointList);
        Double avg4d1Points = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList));
        Double stdev = MathUtils.stdev(pointList);

        NSAD nsad = new NSAD(null, 0, 0, avgPoints, avg4d1Points, stdev, fullPointEntity.isTeam() ? 1d : 0);

        double output = gradientBoostedTreesModel.predict(nsad.toLabeledPoint().features());

        if (isSprint) {
            output = output * 1.19;
        }
        return output;
    }

    private static int getActualPoints(FullPointEntity fullPointEntity, String raceName) {
        return fullPointEntity.getRaceList().stream().filter(r -> r.name().equals(raceName)).findFirst().orElseThrow().totalPoints().intValue();
    }

    private List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }
}
