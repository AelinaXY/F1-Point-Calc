package org.f1.controller.model.response;

import org.f1.model.prediction.PredictionStepTrace;
import org.f1.model.prediction.PredictionTrace;
import org.f1.model.prediction.PredictionTreeTrace;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PredictionTraceSummaryMapper {
    private static final int TOP_FEATURE_LIMIT = 5;

    public PredictionTraceSummaryResponse summarize(PredictionTrace trace) {
        Map<String, FeatureInfluenceAccumulator> influenceMap = new HashMap<>();

        for (PredictionTreeTrace treeTrace : trace.treeTraces()) {
            List<PredictionStepTrace> path = treeTrace.path();
            if (path.isEmpty()) {
                continue;
            }
            double stepImpact = treeTrace.weightedContribution() / path.size();
            for (PredictionStepTrace step : path) {
                FeatureInfluenceAccumulator accumulator = influenceMap.computeIfAbsent(
                        step.featureName(),
                        key -> new FeatureInfluenceAccumulator()
                );
                accumulator.netImpact += stepImpact;
                accumulator.decisionCount++;
                if (Math.abs(stepImpact) > Math.abs(accumulator.strongestStepImpact)) {
                    accumulator.strongestStepImpact = stepImpact;
                    accumulator.strongestReason = buildReason(step);
                }
            }
        }

        List<PredictionFeatureInfluenceResponse> topInfluences = influenceMap.entrySet().stream()
                .map(entry -> toFeatureInfluenceResponse(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Double.compare(Math.abs(b.netImpact()), Math.abs(a.netImpact())))
                .limit(TOP_FEATURE_LIMIT)
                .toList();

        return new PredictionTraceSummaryResponse(
                roundTo2dp(trace.rawPrediction()),
                topInfluences
        );
    }

    private PredictionFeatureInfluenceResponse toFeatureInfluenceResponse(String featureName,
                                                                          FeatureInfluenceAccumulator accumulator) {
        return new PredictionFeatureInfluenceResponse(
                featureName,
                roundTo2dp(accumulator.netImpact),
                accumulator.netImpact >= 0 ? "up" : "down",
                accumulator.decisionCount,
                accumulator.strongestReason
        );
    }

    private String buildReason(PredictionStepTrace step) {
        if ("continuous".equals(step.splitType()) && step.threshold() != null) {
            String branchDirection = step.wentLeft() ? "<=" : ">";
            return "value %.3f %s threshold %.3f".formatted(step.featureValue(), branchDirection, step.threshold());
        }

        if ("categorical".equals(step.splitType())) {
            String branchDirection = step.wentLeft() ? "matched" : "did not match";
            return "value %.3f %s allowed categories".formatted(step.featureValue(), branchDirection);
        }

        return "tree split decision";
    }

    public static double roundTo2dp(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class FeatureInfluenceAccumulator {
        private double netImpact = 0.0;
        private int decisionCount = 0;
        private double strongestStepImpact = 0.0;
        private String strongestReason = "tree split decision";
    }
}
