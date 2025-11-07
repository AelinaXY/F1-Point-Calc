package org.f1.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.f1.calculations.ScoreCalculator;
import org.f1.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class NSAD {
    Integer id;
    int meetingEntityReference;
    Integer actualPoints;
    Double avgPoints;
    Double avg4d1Points;
    Double stdev;
    Double isTeam;
    Double isSprint;
    Double teamId;

    public LabeledPoint toLabeledPoint() {
        Vector vector = new DenseVector(new double[]{avgPoints, avg4d1Points, stdev, isTeam, isSprint, teamId});
        return new LabeledPoint(actualPoints, vector);
    }

    public static NSAD buildFullNSAD(FullPointEntity fullPointEntity, String raceName, int meetingEntityReference, boolean isSprint, double teamId) {
        NSAD basicNSAD = buildBaseNSAD(fullPointEntity, raceName, isSprint, teamId);
        Integer actualPoints = fullPointEntity.getRaceList().stream().filter(r -> r.name().equals(raceName)).findFirst().orElseThrow().totalPoints().intValue();

        basicNSAD.setActualPoints(actualPoints);
        basicNSAD.setMeetingEntityReference(meetingEntityReference);

        return basicNSAD;
    }


    public static NSAD buildBaseNSAD(FullPointEntity fullPointEntity, String raceName, boolean isSprint, double teamId) {
        List<Double> pointList = getListOfPoints(fullPointEntity.getRaceList(), raceName);
        Double avgPoints = ScoreCalculator.calcAveragePoints(pointList);
        Double avg4d1Points = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList));
        Double stdev = MathUtils.stdev(pointList);

        return new NSAD(null, 0, 0, avgPoints, avg4d1Points, stdev, fullPointEntity.isTeam() ? 1d : 0, isSprint ? 1d : 0, teamId);
    }

    private static List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }

}
