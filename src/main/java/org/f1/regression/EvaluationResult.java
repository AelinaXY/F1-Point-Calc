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

@Data
public class EvaluationResult {
    private static final HyperParameters CONTROL_HYPERPARAMETERS = new HyperParameters(225, 2, 0.06, 4, 0.87);
    private final HyperParameters hyperParameters;
    private final double meanSquaredError;
    private static final int numFolds = 5;
    private static final long SEED_BASE = 42L;


    public static EvaluationResult parallelGridSearch(Dataset<Row> dataSet) throws IOException {
        Logger logger = getEvaluationResultLogger();

        List<HyperParameters> paramGrid = generateParameterGrid();

        //Old best
        paramGrid.addFirst(CONTROL_HYPERPARAMETERS);
        dataSet.cache();
        dataSet.count();

        List<Dataset<Row>[]> folds = getFolds(dataSet);

        long startTime = System.currentTimeMillis();
        logger.info("Starting two-stage hyperparameter tuning");
        logger.info("STAGE 1: Shallow pass on all " + paramGrid.size() + " parameter combinations");

        // Stage 1: Shallow pass with single fold evaluation
        Set<EvaluationResult> shallowResults = evaluateHyperparameters(paramGrid, folds.subList(0,1) , logger, startTime);

        logger.info("STAGE 1 Complete. Identifying top 5 candidates");

        // Get top 5 parameter combinations by MSE
        List<HyperParameters> topParams = shallowResults.stream()
                .sorted(Comparator.comparingDouble(EvaluationResult::getMeanSquaredError))
                .limit(5)
                .map(EvaluationResult::getHyperParameters)
                .collect(Collectors.toList());

        topParams.addFirst(CONTROL_HYPERPARAMETERS);

        logger.info("Top 5 candidates selected plus control. Starting STAGE 2: Full cross-validation.");

        // Stage 2: Full evaluation with 3-fold cross-validation on top 5 and control
        Set<EvaluationResult> deepResults = evaluateHyperparameters(topParams, folds, logger, startTime);

        dataSet.unpersist();

        Optional<EvaluationResult> bestResult = deepResults.stream().reduce((r1, r2) ->
                r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);

        logger.info("Hyperparameter tuning complete.");
        return bestResult.orElseThrow(() -> new RuntimeException("No results found"));
    }

    private static List<Dataset<Row>[]> getFolds(Dataset<Row> dataSet) {
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
        return folds;
    }

    private static Set<EvaluationResult> evaluateHyperparameters(List<HyperParameters> inputParams, List<Dataset<Row>[]> folds, Logger logger, long startTime) {
        RegressionEvaluator mseEvaluator = getEvaluator("mse");

        AtomicInteger count = new AtomicInteger(0);
        int paramCount = inputParams.size();

        return inputParams.stream().map(params -> {
            int currentCount = count.incrementAndGet();

            double mse = 0.0;

            int numFolds = folds.size();

            for (Dataset<Row>[] fold : folds) {
                Dataset<Row> training = fold[0];
                Dataset<Row> testing = fold[1];

                GBTRegressionModel model = buildRegressor(params).fit(training);
                Dataset<Row> predictions = model.transform(testing).cache();

                mse += mseEvaluator.evaluate(predictions);
                predictions.unpersist();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            String timeStr = String.format("%d:%02d:%02d", elapsed / 3600000, (elapsed % 3600000) / 60000, (elapsed % 60000) / 1000);

            logger.info(String.format("Pass: %03d/%03d | Time: %s | Avg MSE: %f | Params: %s",
                    currentCount, paramCount, timeStr, mse / numFolds, params));

            return new EvaluationResult(
                    params,
                    mse / numFolds
            );
        }).collect(Collectors.toSet());
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

        int[] numIterations = {210, 225};
        int[] maxDepths = {4};
        double[] learningRates = {0.0525, 0.055, 0.060};
        int[] minInstancesPerNode = {2, 3, 4, 6};
        double[] subsamplingRates = {0.87, 0.90, 0.925};

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
