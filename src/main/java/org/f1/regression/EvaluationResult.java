package org.f1.regression;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.GradientBoostedTrees;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.service.RegressionService;
import org.jetbrains.annotations.NotNull;
import scala.Tuple2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

@Slf4j
@Data
public class EvaluationResult {
    private final HyperParameters hyperParameters;
    private final double meanSquaredError;
    private final double meanAbsoluteError;
    private final double rSquared;
    private static final int numFolds = 5;


    public static EvaluationResult parallelGridSearch(JavaRDD<LabeledPoint> dataSet, JavaSparkContext sparkContext, Map<Integer, Integer> categoricalFeaturesInfo) throws IOException {
        Logger logger = getEvaluationResultLogger();

        List<HyperParameters> paramGrid = generateParameterGrid();
        dataSet.cache();

        AtomicInteger count = new AtomicInteger(0);

        Set<EvaluationResult> results = paramGrid.parallelStream().map(params -> {
            int currentCount = count.incrementAndGet();

            double totalMSE = 0.0;
            double totalMAE = 0.0;
            double totalR2 = 0.0;

            for (int fold = 0; fold < numFolds; fold++) {
                JavaRDD<LabeledPoint>[] splits = dataSet.randomSplit(
                        new double[]{0.8, 0.2}, fold * 1000L + System.currentTimeMillis()

                );
                JavaRDD<LabeledPoint> training = splits[0];
                JavaRDD<LabeledPoint> testing = splits[1];

                try {
                    BoostingStrategy boostingStrategy = BoostingStrategy.defaultParams("Regression");
                    boostingStrategy.setNumIterations(params.getNumIterations());
                    boostingStrategy.setLearningRate(params.getLearningRate());
                    boostingStrategy.treeStrategy().setMaxDepth(params.getMaxDepth());
                    boostingStrategy.treeStrategy().setSubsamplingRate(params.getSubsamplingRate());
                    boostingStrategy.treeStrategy().setCategoricalFeaturesInfo(categoricalFeaturesInfo);
                    boostingStrategy.treeStrategy().setMinInstancesPerNode(params.getMinInstancesPerNode());
                    boostingStrategy.treeStrategy().setMinInfoGain(0.01);
                    boostingStrategy.treeStrategy().setMaxBins(32);

                    GradientBoostedTreesModel model = GradientBoostedTrees.train(training, boostingStrategy);

                    JavaPairRDD<Double, Double> predictionAndLabel = testing.mapToPair(
                            p -> new Tuple2<>(model.predict(p.features()), p.label())
                    );

                    totalMSE += getMeanSquaredError(predictionAndLabel);
                    totalMAE += getMeanAbsoluteError(predictionAndLabel);
                    totalR2 += getRSquared(predictionAndLabel);
                } finally {
                    training.unpersist();
                    testing.unpersist();
                }
            }
            logger.info(String.format("Version %d of %d. Params of %s. Total mean error: %f", currentCount, paramGrid.size(), params, totalMSE / numFolds));
            return new EvaluationResult(
                    params,
                    totalMSE / numFolds,
                    totalMAE / numFolds,
                    totalR2 / numFolds
            );
        }).collect(Collectors.toSet());


        Optional<EvaluationResult> bestResult = results.stream().reduce((r1, r2) ->
                r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);

        dataSet.unpersist();

        return bestResult.orElseThrow(() -> new RuntimeException("No results found"));
    }

    private static List<HyperParameters> generateParameterGrid() {
        List<HyperParameters> paramGrid = new ArrayList<>();

        int[] numIterations = {25, 50, 100};
        int[] maxDepths = {3, 5, 7};
        double[] learningRates = {0.01, 0.03, 0.1};
        int[] minInstancesPerNode = {1, 3, 5};
        double[] subsamplingRates = {0.8, 1.0};

        for (int numIterationsValue : numIterations) {
            for (int maxDepthValue : maxDepths) {
                for (double learningRateValue : learningRates) {
                    for (int minInstancesPerNodeValue : minInstancesPerNode) {
                        for (double subsamplingRateValue : subsamplingRates) {
                            paramGrid.add(new HyperParameters(numIterationsValue,
                                    maxDepthValue,
                                    learningRateValue,
                                    minInstancesPerNodeValue,
                                    subsamplingRateValue));
                        }
                    }
                }
            }
        }
        return paramGrid;
    }

    private static double getMeanSquaredError(JavaPairRDD<Double, Double> predictionAndLabel) {
        return predictionAndLabel.mapToDouble(pair ->
                        Math.pow(pair._1 - pair._2, 2))
                .mean();
    }

    private static double getMeanAbsoluteError(JavaPairRDD<Double, Double> predictionAndLabel) {
        return predictionAndLabel.mapToDouble(pair ->
                Math.abs(pair._1() - pair._2())).mean();
    }

    private static double getRSquared(JavaPairRDD<Double, Double> predictionAndLabel) {
        double mean = predictionAndLabel.mapToDouble(Tuple2::_2).mean();
        double totalSS = predictionAndLabel.mapToDouble(pl ->
                        Math.pow(pl._2() - mean, 2))
                .sum();
        double residualSS = predictionAndLabel.mapToDouble(pl ->
                Math.pow(pl._1() - pl._2(), 2)).sum();
        return 1 - (residualSS / totalSS);
    }

    private static @NotNull Logger getEvaluationResultLogger() throws IOException {
        Logger logger = Logger.getLogger(RegressionService.class.getName());
        FileHandler fh;
        fh = new FileHandler("/Users/lauren.darlaston/workspace/F1-Point-Calc/machineLearning.log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        return logger;
    }
}
