package org.f1.model.prediction;

import java.util.List;

public record PredictionStepTrace(
        Integer depth,
        Integer featureIndex,
        String featureName,
        Double featureValue,
        String splitType,
        Double threshold,
        List<Double> leftCategories,
        Boolean wentLeft
) {
}
