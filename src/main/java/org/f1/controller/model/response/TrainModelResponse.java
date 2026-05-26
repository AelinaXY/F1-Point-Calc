package org.f1.controller.model.response;

import com.alibaba.fastjson2.JSONObject;
import org.f1.regression.HyperParameters;

import java.util.Map;

public class TrainModelResponse {

    private final HyperParameters hyperParameters;
    private final double mse;
    private final Map<String, Double> featureImportance;


    public TrainModelResponse(HyperParameters hyperParameters, double mse, Map<String, Double> featureImportance) {
        this.hyperParameters = hyperParameters;
        this.mse = mse;
        this.featureImportance = featureImportance;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject
                .fluentPut("hyperParameters",
                        new JSONObject()
                                .fluentPut("numIterations", hyperParameters.getNumIterations())
                                .fluentPut("learningRate", hyperParameters.getLearningRate())
                                .fluentPut("maxDepth", hyperParameters.getMaxDepth())
                                .fluentPut("minInstancesPerNode", hyperParameters.getMinInstancesPerNode())
                                .fluentPut("subSamplingRate", hyperParameters.getSubsamplingRate())
                                .fluentPut("maxBins", hyperParameters.getMaxBin())
                                .fluentPut("minInfoGain", hyperParameters.getMinInfoGain()))
                .fluentPut("meanSquaredError", mse)
                .fluentPut("featureImportance", featureImportance);
        return jsonObject;
    }


}
