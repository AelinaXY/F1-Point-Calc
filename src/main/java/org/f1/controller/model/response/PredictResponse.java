package org.f1.controller.model.response;

public record PredictResponse(Double predictedPoints, Double predictedCostChange, boolean isTeam) {
}
