package org.f1.regression;

import lombok.Data;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.regression.GBTRegressor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.f1.service.RegressionService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class EvaluationResult {
    private final HyperParameters hyperParameters;
    private final double meanSquaredError;
    private final double meanAbsoluteError;
    private final double rSquared;
    private static final int numFolds = 3;
    private static final long SEED_BASE = 42L;


    public static EvaluationResult parallelGridSearch(Dataset<Row> dataSet) throws IOException {
        Logger logger = getEvaluationResultLogger();

        List<HyperParameters> paramGrid = generateParameterGrid();

        //Old best
        paramGrid.addFirst(new HyperParameters(100, 3, 0.03, 1, 0.8));
        dataSet.cache();
        dataSet.count();

        List<Dataset<Row>[]> folds = new ArrayList<>();
        for (int i = 0; i < numFolds; i++) {
            Dataset<Row>[] splits = dataSet.randomSplit(
                    new double[]{0.8, 0.2}, i * 1000L + SEED_BASE
            );
            splits[0].cache();
            splits[0].count();
            splits[1].cache();
            splits[1].count();
            folds.add(splits);
        }

        AtomicInteger count = new AtomicInteger(0);
        RegressionEvaluator mseEvaluator = getEvaluator("mse");
        RegressionEvaluator maeEvaluator = getEvaluator("mae");
        RegressionEvaluator r2Evaluator = getEvaluator("r2");

        Set<EvaluationResult> results = paramGrid.stream().flatMap(params -> {
            int currentCount = count.incrementAndGet();

            double[] metrics = new double[3];

            for (Dataset<Row>[] fold : folds) {
                Dataset<Row> training = fold[0];
                Dataset<Row> testing = fold[1];

                GBTRegressionModel model = buildRegressor(params).fit(training);
                Dataset<Row> predictions = model.transform(testing).cache();

                metrics[0] += mseEvaluator.evaluate(predictions);
                metrics[1] += maeEvaluator.evaluate(predictions);
                metrics[2] += r2Evaluator.evaluate(predictions);
                predictions.unpersist();

            }

            logger.info(String.format("Version %d of %d | Total mean error: %f | Params of %s.",
                    currentCount, paramGrid.size(), metrics[0] / numFolds, params));

            return Stream.of(new EvaluationResult(
                    params,
                    metrics[0] / numFolds,
                    metrics[1] / numFolds,
                    metrics[2] / numFolds
            ));
        }).collect(Collectors.toSet());


        Optional<EvaluationResult> bestResult = results.stream().reduce((r1, r2) ->
                r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);

        dataSet.unpersist();

        return bestResult.orElseThrow(() -> new RuntimeException("No results found"));
    }

    public static GBTRegressor buildRegressor(HyperParameters params) {
        return new GBTRegressor()
                .setLabelCol("label")
                .setFeaturesCol("features")
                .setPredictionCol("prediction")
                .setMaxIter(params.getNumIterations())
                .setStepSize(params.getLearningRate())
                .setMaxDepth(params.getMaxDepth())
                .setSubsamplingRate(params.getSubsamplingRate())
                .setMinInstancesPerNode(params.getMinInstancesPerNode())
                .setMinInfoGain(0.01)
                .setMaxBins(32);
    }

    private static RegressionEvaluator getEvaluator(String metricName) {
        return new RegressionEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName(metricName);
    }

    private static List<HyperParameters> generateParameterGrid() {
        List<HyperParameters> paramGrid = new ArrayList<>();

        int[] numIterations = {100, 125, 150, 200, 250};
        int[] maxDepths = {2, 3};
        double[] learningRates = {0.035, 0.05, 0.065};
        int[] minInstancesPerNode = {3, 5, 8, 10};
        double[] subsamplingRates = {0.8, 0.85, 0.9};

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

    private static Logger getEvaluationResultLogger() throws IOException {
        Logger logger = Logger.getLogger(RegressionService.class.getName());
        FileHandler fh = new FileHandler("/Users/lauren.darlaston/workspace/F1-Point-Calc/machineLearning.log");

        fh.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return record.getMessage() + System.lineSeparator();
            }
        });
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);

        return logger;
    }
}
