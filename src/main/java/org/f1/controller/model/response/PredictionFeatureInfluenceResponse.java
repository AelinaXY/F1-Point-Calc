package org.f1.controller.model.response;

public record PredictionFeatureInfluenceResponse(
        String featureName,
        Double netImpact,
        String movement,
        Integer decisionCount,
        Double strongestImpact,
        String reason
) {
}
