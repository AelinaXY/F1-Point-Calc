package org.f1.model.prediction;

import java.util.List;
import java.util.Map;

public record PredictionTrace(
        Double rawPrediction,
        Map<String, Double> featureValues,
        List<Double> treeContributions,
        List<Double> runningTotals,
        List<Integer> leafIds,
        List<PredictionTreeTrace> treeTraces
) {
}
