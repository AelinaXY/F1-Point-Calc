package org.f1.regression;

import lombok.Data;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.regression.GBTRegressor;
import org.apache.spark.ml.tuning.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.f1.service.RegressionService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

@Data
public class EvaluationResult {
    private final GBTRegressionModel trainedModel;
    private final double meanSquaredError;
    private static final int numFolds = 2;
    private static final long SEED_BASE = 42L;


    public static EvaluationResult parallelGridSearch(Dataset<Row> dataSet) throws IOException {
        Logger logger = getEvaluationResultLogger();

        dataSet.cache();
        dataSet.count();

        logger.info("Starting two-stage hyperparameter tuning");
        logger.info("STAGE 1: Shallow pass on entire parameter grid");
        GBTRegressor estimator = getEstimator();
        ParamMap[] paramGrid = getParamGrid(estimator);

        ParamMap[] top10Params = getTop10Params(dataSet, paramGrid, estimator);
        logger.info("Top 10 parameters identified");
        logger.info("STAGE 2: Full cross-validation on top 10 parameters");

        CrossValidatorModel crossValModel = crossValidate(dataSet, top10Params, estimator);

        logger.info("STAGE 2 Complete. Best model identified");
        GBTRegressionModel output = (GBTRegressionModel) crossValModel.bestModel();

        dataSet.unpersist();

        return new EvaluationResult(output, Arrays.stream(crossValModel.avgMetrics()).min().orElseThrow());
    }

    public static Map<String, Object> getBestParams(GBTRegressionModel model) {
        Map<String, Object> hyperParams = new LinkedHashMap<>();

        hyperParams.put("numIterations", model.getMaxIter());
        hyperParams.put("learningRate", model.getStepSize());
        hyperParams.put("maxDepth", model.getMaxDepth());
        hyperParams.put("minInstancesPerNode", model.getMinInstancesPerNode());
        hyperParams.put("subSamplingRate", model.getSubsamplingRate());
        return hyperParams;
    }

    private static CrossValidatorModel crossValidate(Dataset<Row> dataSet, ParamMap[] paramGrid, GBTRegressor estimator) {
        CrossValidator crossVal = new CrossValidator()
                .setEstimator(estimator)
                .setEvaluator(getEvaluator("mse"))
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(numFolds)
                .setSeed(SEED_BASE)
                .setParallelism(1);

        return crossVal.fit(dataSet);
    }

    private static ParamMap[] getParamGrid(GBTRegressor estimator) {
        int[] numIterations = {175, 190, 210, 225};
        int[] maxDepths = {2};
        double[] learningRates = {0.0525, 0.055, 0.060};
        int[] minInstancesPerNode = {2, 3, 4, 6};
        double[] subsamplingRates = {0.87, 0.90, 0.925};
        
        return new ParamGridBuilder().addGrid(estimator.maxIter(), numIterations)
                .addGrid(estimator.maxDepth(), maxDepths)
                .addGrid(estimator.stepSize(), learningRates)
                .addGrid(estimator.minInstancesPerNode(), minInstancesPerNode)
                .addGrid(estimator.subsamplingRate(), subsamplingRates)
                .build();
    }

    private static ParamMap[] getTop10Params(Dataset<Row> dataSet, ParamMap[] paramGrid, GBTRegressor estimator) {
        TrainValidationSplit trainValidationSplit = new TrainValidationSplit()
                .setEstimator(estimator)
                .setEvaluator(getEvaluator("mse"))
                .setEstimatorParamMaps(paramGrid)
                .setTrainRatio(0.8)
                .setSeed(SEED_BASE)
                .setParallelism(1);

        TrainValidationSplitModel initialValidationModel = trainValidationSplit.fit(dataSet);

        ParamMap[] paramMaps = initialValidationModel.getEstimatorParamMaps();
        double[] validationMetrics = initialValidationModel.validationMetrics();

        if(paramMaps.length != validationMetrics.length) {
            throw new RuntimeException("paramMaps and validationMetrics are not the same length. Something is very wrong.");
        }

        List<AbstractMap.SimpleEntry<ParamMap, Double>> paramMapAndValidationMetrics = new ArrayList<>();

        for(int i = 0; i < paramMaps.length; i++) {
            paramMapAndValidationMetrics.add(new AbstractMap.SimpleEntry<>(paramMaps[i], validationMetrics[i]));
        }

        paramMapAndValidationMetrics.sort(Comparator.comparingDouble(Map.Entry::getValue));

        return paramMapAndValidationMetrics.stream().limit(10).map(Map.Entry::getKey).toArray(ParamMap[]::new);
    }

    private static GBTRegressor getEstimator() {
        return new GBTRegressor().setLabelCol("label").setFeaturesCol("features").setPredictionCol("prediction");
    }
    
    private static RegressionEvaluator getEvaluator(String metricName) {
        return new RegressionEvaluator()
                .setLabelCol("label")
                .setPredictionCol("prediction")
                .setMetricName(metricName);
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
