package org.f1.model.prediction;

import java.util.List;

public record PredictionTreeTrace(
        Integer treeIndex,
        Double treeWeight,
        Double rawTreePrediction,
        Double weightedContribution,
        Double runningTotalAfterTree,
        Integer leafId,
        List<PredictionStepTrace> path
) {
}
