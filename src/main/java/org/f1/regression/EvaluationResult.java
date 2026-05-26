package org.f1.regression;

import lombok.Data;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.regression.GBTRegressor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.f1.domain.NSAD;
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
    private static final HyperParameters CONTROL_HYPERPARAMETERS = new HyperParameters(225, 2, 0.06, 4, 0.87, 32, 0.01);
    private final HyperParameters hyperParameters;
    private final double meanSquaredError;
    private static final int numFolds = 5;
    private static final long SEED_BASE = 42L;


    public static EvaluationResult parallelGridSearch(List<NSAD> dataSet, SparkSession sparkSession) throws IOException {
        Logger logger = getEvaluationResultLogger();

        List<HyperParameters> paramGrid = generateParameterGrid();

        //Old best
        paramGrid.addFirst(CONTROL_HYPERPARAMETERS);


        List<List<Dataset<Row>>> folds = getFolds(dataSet, sparkSession);

        long startTime = System.currentTimeMillis();
        logger.info("Starting two-stage hyperparameter tuning");
        logger.info("STAGE 1: Shallow pass on all " + paramGrid.size() + " parameter combinations");

        // Stage 1: Shallow pass with single fold evaluation
        Set<EvaluationResult> shallowResults = evaluateHyperparameters(paramGrid, folds.subList(0, 1), logger, startTime);

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

        Optional<EvaluationResult> bestResult = deepResults.stream().reduce((r1, r2) ->
                r1.getMeanSquaredError() < r2.getMeanSquaredError() ? r1 : r2);

        logger.info("Hyperparameter tuning complete.");
        return bestResult.orElseThrow(() -> new RuntimeException("No results found"));
    }

    private static List<List<Dataset<Row>>> getFolds(List<NSAD> dataSet, SparkSession sparkSession) {
        List<List<Dataset<Row>>> folds = new ArrayList<>();
        Map<Integer, List<NSAD>> meetingToNsadMap = dataSet
                .stream()
                .collect(Collectors.groupingBy(nsad -> nsad.getMeetingEntityReference().getMeetingId()));
        List<Integer> meetingIds = new ArrayList<>(meetingToNsadMap.keySet());
        int trainingSize = Math.toIntExact(Math.round(meetingIds.size() * 0.8));
        for (int i = 0; i < numFolds; i++) {
            Collections.shuffle(meetingIds, new Random(i * 1000 + SEED_BASE));

            List<Integer> trainingIds = meetingIds.subList(0, trainingSize);
            List<Integer> testingIds = meetingIds.subList(trainingSize, meetingIds.size());

            List<Row> trainingData = trainingIds.stream().map(meetingToNsadMap::get).flatMap(Collection::stream).map(NSAD::toRegressionRow).toList();
            List<Row> testingData = testingIds.stream().map(meetingToNsadMap::get).flatMap(Collection::stream).map(NSAD::toRegressionRow).toList();

            List<Dataset<Row>> splits = List.of(
                    sparkSession.createDataFrame(trainingData, NSAD.regressionSchema()),
                    sparkSession.createDataFrame(testingData, NSAD.regressionSchema())
            );

            splits.get(0).cache();
            splits.get(0).count();
            splits.get(1).cache();
            splits.get(1).count();
            folds.add(splits);
        }
        return folds;
    }

    private static Set<EvaluationResult> evaluateHyperparameters(List<HyperParameters> inputParams, List<List<Dataset<Row>>> folds, Logger logger, long startTime) {
        RegressionEvaluator mseEvaluator = getEvaluator("mse");

        AtomicInteger count = new AtomicInteger(0);
        int paramCount = inputParams.size();

        return inputParams.stream().map(params -> {
            int currentCount = count.incrementAndGet();

            double mse = 0.0;

            int numFolds = folds.size();

            for (List<Dataset<Row>> fold : folds) {
                Dataset<Row> training = fold.get(0);
                Dataset<Row> testing = fold.get(1);

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
                .setMinInfoGain(params.getMinInfoGain())
                .setMaxBins(params.getMaxBin());
    }

    private static RegressionEvaluator getEvaluator(String metricName) {
        return new RegressionEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName(metricName);
    }

    private static List<HyperParameters> generateParameterGrid() {
        List<HyperParameters> paramGrid = new ArrayList<>();

        int[] numIterations = {125, 150, 175};
        int[] maxDepths = {2, 3};
        double[] learningRates = {0.04, 0.05, 0.06};
        int[] minInstancesPerNode = {8, 12, 16, 24};
        double[] subsamplingRates = {0.6, 0.7, 0.8};
        int[] maxBins = {32};
        double[] minInfoGains = {0.01};

        for (int numIterationsValue : numIterations) {
            for (int maxDepthValue : maxDepths) {
                for (double learningRateValue : learningRates) {
                    for (int minInstancesPerNodeValue : minInstancesPerNode) {
                        for (double subsamplingRateValue : subsamplingRates) {
                            for (int maxBin : maxBins) {
                                for (double minInfoGain : minInfoGains) {
                                    paramGrid.add(new HyperParameters(numIterationsValue,
                                            maxDepthValue,
                                            learningRateValue,
                                            minInstancesPerNodeValue,
                                            subsamplingRateValue,
                                            maxBin,
                                            minInfoGain));
                                }
                            }

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
