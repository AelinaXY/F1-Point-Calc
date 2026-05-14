package org.f1.controller.model.response;

import com.alibaba.fastjson2.JSONObject;
import org.apache.spark.ml.param.ParamMap;
import org.f1.regression.HyperParameters;

import java.util.Map;

public class TrainModelResponse {

    private final Map<String, Object> hyperParameters;
    private final double mse;
    private final Map<String, Double> featureImportance;


    public TrainModelResponse(Map<String, Object> hyperParameters, double mse, Map<String, Double> featureImportance) {
        this.hyperParameters = hyperParameters;
        this.mse = mse;
        this.featureImportance = featureImportance;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject
                .fluentPut("hyperParameters",
                        new JSONObject(hyperParameters))
                .fluentPut("meanSquaredError", mse)
                .fluentPut("featureImportance", featureImportance);
        return jsonObject;
    }


}
