package org.f1.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.spark.ml.attribute.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
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
    Integer daysSinceFirstRace;

    public Row toRegressionRow() {
        return RowFactory.create(actualPoints.doubleValue(), toFeaturesVector());
    }

    public Vector toFeaturesVector() {
        return Vectors.dense(avgPoints, avg4d1Points, stdev, isTeam, isSprint, teamId);
    }

    public static StructType regressionSchema() {
        Attribute[] attributes = new Attribute[]{
                NumericAttribute.defaultAttr().withName("Average Points"),
                NumericAttribute.defaultAttr().withName("4-Race Average"),
                NumericAttribute.defaultAttr().withName("Standard Deviation"),
                BinaryAttribute.defaultAttr().withName("Is Team"),
                BinaryAttribute.defaultAttr().withName("Is Sprint"),
                NominalAttribute.defaultAttr().withName("Team ID").withNumValues(12),
                NumericAttribute.defaultAttr().withName("Days Since First Race")
        };

        StructField featuresField = new AttributeGroup("features", attributes).toStructField();
        return new StructType(new StructField[]{
                DataTypes.createStructField("label", DataTypes.DoubleType, false),
                featuresField
        });
    }

    public static NSAD buildFullNSAD(FullPointEntity fullPointEntity, String raceName, int meetingEntityReference, boolean isSprint, double teamId, Integer daysSinceFirstRace) {
        NSAD basicNSAD = buildUnlabelledNSAD(fullPointEntity, raceName, isSprint, teamId, daysSinceFirstRace);
        Integer actualPoints = fullPointEntity.getRaceList().stream().filter(r -> r.name().equals(raceName)).findFirst().orElseThrow().totalPoints().intValue();

        basicNSAD.setActualPoints(actualPoints);
        basicNSAD.setMeetingEntityReference(meetingEntityReference);

        return basicNSAD;
    }


    public static NSAD buildUnlabelledNSAD(FullPointEntity fullPointEntity, String raceName, boolean isSprint, double teamId, int daysSinceFirstRace) {
        List<Double> pointList = getListOfPoints(fullPointEntity.getRaceList(), raceName);
        Double avgPoints = ScoreCalculator.calcAveragePoints(pointList);
        Double avg4d1Points = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList));
        Double stdev = MathUtils.stdev(pointList);

        return new NSAD(null, 0, 0, avgPoints, avg4d1Points, stdev, fullPointEntity.isTeam() ? 1d : 0, isSprint ? 1d : 0, teamId, daysSinceFirstRace);
    }

    private static List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }

}
