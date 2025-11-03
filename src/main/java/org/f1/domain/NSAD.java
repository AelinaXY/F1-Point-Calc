package org.f1.domain;


import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

public record NSAD(Integer id,
                   int meetingEntityReference,
                   Integer actualPoints,
                   Double avgPoints,
                   Double avg4d1Points,
                   Double stdev,
                   Double isTeam) {


    public LabeledPoint toLabeledPoint() {
        Vector vector = new DenseVector(new double[]{avgPoints, avg4d1Points, stdev, isTeam});
        return new LabeledPoint(actualPoints, vector);
    }
}
