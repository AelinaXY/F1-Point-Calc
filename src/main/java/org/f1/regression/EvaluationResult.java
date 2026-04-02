package org.f1.regression;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.GradientBoostedTrees;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.service.RegressionService;
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

        List<Tuple2<JavaRDD<LabeledPoint>, JavaRDD<LabeledPoint>>> folds = new ArrayList<>();
        long seedBase = System.currentTimeMillis();
        for (int i = 0; i < numFolds; i++) {
            JavaRDD<LabeledPoint>[] splits = dataSet.randomSplit(
                    new double[]{0.8, 0.2}, i * 1000L + seedBase
            );
            folds.add(new Tuple2<>(splits[0], splits[1]));
        }

        AtomicInteger count = new AtomicInteger(0);

        Set<EvaluationResult> results = paramGrid.parallelStream().flatMap(params -> {
            int currentCount = count.incrementAndGet();

            Map<Integer, double[]> metricsMap = new HashMap<>();
            int[] iterationsToCheck = {25, 50, 100};
            for (int iter : iterationsToCheck) {
                metricsMap.put(iter, new double[3]);
            }

            for (Tuple2<JavaRDD<LabeledPoint>, JavaRDD<LabeledPoint>> fold : folds) {
                JavaRDD<LabeledPoint> training = fold._1();
                JavaRDD<LabeledPoint> testing = fold._2();

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

                for (int iter : iterationsToCheck) {
                    GradientBoostedTreesModel slicedModel = (iter == params.getNumIterations()) ?
                            model : sliceModel(model, iter);

                    JavaPairRDD<Double, Double> predictionAndLabel = testing.mapToPair(
                            p -> new Tuple2<>(slicedModel.predict(p.features()), p.label())
                    );

                    double[] m = metricsMap.get(iter);
                    m[0] += getMeanSquaredError(predictionAndLabel);
                    m[1] += getMeanAbsoluteError(predictionAndLabel);
                    m[2] += getRSquared(predictionAndLabel);
                }
            }

            return Arrays.stream(iterationsToCheck).mapToObj(iter -> {
                double[] m = metricsMap.get(iter);
                HyperParameters currentParams = (iter == params.getNumIterations()) ? params :
                        new HyperParameters(iter, params.getMaxDepth(), params.getLearningRate(),
                                params.getMinInstancesPerNode(), params.getSubsamplingRate());

                if (iter == 100) {
                    logger.info(String.format("Version %d of %d. Params of %s. Total mean error: %f",
                            currentCount, paramGrid.size(), currentParams, m[0] / numFolds));
                }

                return new EvaluationResult(
                        currentParams,
                        m[0] / numFolds,
                        m[1] / numFolds,
                        m[2] / numFolds
                );
            });
        }).collect(Collectors.toSet());


        Optional<EvaluationResult> bestResult = results.stream().reduce((r1, r2) ->
                r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);

        dataSet.unpersist();

        return bestResult.orElseThrow(() -> new RuntimeException("No results found"));
    }

    private static GradientBoostedTreesModel sliceModel(GradientBoostedTreesModel model, int numTrees) {
        DecisionTreeModel[] trees = Arrays.copyOf(model.trees(), numTrees);
        double[] weights = Arrays.copyOf(model.treeWeights(), numTrees);
        return new GradientBoostedTreesModel(model.algo(), trees, weights);
    }

    private static List<HyperParameters> generateParameterGrid() {
        List<HyperParameters> paramGrid = new ArrayList<>();

        int[] numIterations = {100};
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

    private static Logger getEvaluationResultLogger() throws IOException {
        Logger logger = Logger.getLogger(RegressionService.class.getName());
        FileHandler fh;
        fh = new FileHandler("/Users/lauren.darlaston/workspace/F1-Point-Calc/machineLearning.log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        return logger;
    }
}
