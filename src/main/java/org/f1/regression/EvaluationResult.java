package org.f1.regression;

import lombok.Data;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.GradientBoostedTrees;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class EvaluationResult {
    private final HyperParameters hyperParameters;
    private final double meanSquaredError;
    private final double meanAbsoluteError;
    private final double rSquared;
    private static final int numFolds = 5;


    public static EvaluationResult parallelGridSearch(JavaRDD<LabeledPoint> dataSet, JavaSparkContext sparkContext, Map<Integer, Integer> categoricalFeaturesInfo) {
        List<HyperParameters> paramGrid = generateParameterGrid();

        JavaRDD<HyperParameters> parameterRDD = sparkContext.parallelize(paramGrid);

        JavaRDD<EvaluationResult> results = parameterRDD.mapPartitions(params -> {
            List<EvaluationResult> partitionResults = new ArrayList<>();
            while (params.hasNext()) {
                HyperParameters paramsValue = params.next();
                EvaluationResult result = evaluateParameters(paramsValue, dataSet, categoricalFeaturesInfo, sparkContext);
                partitionResults.add(result);
            }
            return partitionResults.iterator();
        });

        return results.reduce((r1, r2) -> r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);
    }

    private static EvaluationResult evaluateParameters(HyperParameters params, JavaRDD<LabeledPoint> localDataSet, Map<Integer, Integer> categoricalFeaturesInfo, JavaSparkContext sparkContext) {
        double totalMSE = 0.0;
        double totalMAE = 0.0;
        double totalR2 = 0.0;

        for (int fold = 0; fold < numFolds; fold++) {
            JavaRDD<LabeledPoint>[] splits = localDataSet.randomSplit(
                    new double[]{0.8, 0.2}, fold * 1000L + System.currentTimeMillis()

            );
            JavaRDD<LabeledPoint> training = splits[0];
            JavaRDD<LabeledPoint> testing = splits[1];

            BoostingStrategy boostingStrategy = BoostingStrategy.defaultParams("Regression");
            boostingStrategy.setNumIterations(params.getNumIterations());
            boostingStrategy.setLearningRate(params.getLearningRate());
            boostingStrategy.treeStrategy().setMaxDepth(params.getMaxDepth());
            boostingStrategy.treeStrategy().setSubsamplingRate(params.getSubsamplingRate());
            boostingStrategy.treeStrategy().setCategoricalFeaturesInfo(categoricalFeaturesInfo);
            boostingStrategy.treeStrategy().setMinInstancesPerNode(params.getMinInstancesPerNode());
            boostingStrategy.treeStrategy().setMinInfoGain(0.01);

            GradientBoostedTreesModel model = GradientBoostedTrees.train(
                    training, boostingStrategy
            );

            JavaPairRDD<Double, Double> predictionAndLabel = testing.mapToPair(
                    p -> new Tuple2<>(model.predict(p.features()), p.label())
            );
            totalMSE += getMeanSquaredError(predictionAndLabel);
            totalMAE += getMeanAbsoluteError(predictionAndLabel);
            totalR2 += getRSquared(predictionAndLabel);

        }

        return new EvaluationResult(params, totalMSE / numFolds, totalMAE / numFolds, totalR2 / numFolds);
    }

    private static List<HyperParameters> generateParameterGrid() {
        List<HyperParameters> paramGrid = new ArrayList<>();

        int[] numIterations = {20, 50};
        int[] maxDepths = {3, 5, 7, 9};
        double[] learningRates = {0.01, 0.03, 0.1, 0.3};
        int[] minInstancesPerNode = {1};
        int[] subsamplingRates = {1};

        for (int numIterationsValue : numIterations) {
            for (int maxDepthValue : maxDepths) {
                for (double learningRateValue : learningRates) {
                    for (int minInstancesPerNodeValue : minInstancesPerNode) {
                        for (int subsamplingRateValue : subsamplingRates) {
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
}
