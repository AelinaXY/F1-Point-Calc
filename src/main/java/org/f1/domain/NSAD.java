package org.f1.domain;

import org.apache.spark.ml.feature.LabeledPoint;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.linalg.Vector;

public record NSAD(Integer id,
                   int meetingEntityReference,
                   Integer actualPoints,
                   Double avgPoints,
                   Double avg4d1Points,
                   Double stdev,
                   Double isTeam) {


    public LabeledPoint toLabeledPoint() {
        Vector vector = new DenseVector(new double[]{avgPoints, avg4d1Points, stdev});
        return new LabeledPoint(actualPoints, vector);
    }
}
