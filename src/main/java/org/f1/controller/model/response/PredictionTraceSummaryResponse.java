package org.f1.controller.model.response;

import java.util.List;

public record PredictionTraceSummaryResponse(
        Double rawPrediction,
        List<PredictionFeatureInfluenceResponse> topFeatureInfluences,
        List<PathTraceResponse> pathTraces
) {
}
