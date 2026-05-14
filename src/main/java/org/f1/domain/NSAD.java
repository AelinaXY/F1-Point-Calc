package org.f1.domain;


import lombok.*;
import org.apache.spark.ml.attribute.*;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.IntegerType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.f1.calculations.ScoreCalculator;
import org.f1.generated.tables.records.NonSprintAggregateDataRecord;
import org.f1.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class NSAD {
    private Integer id;
    private int meetingEntityReference;
    private Integer actualPoints;
    private Double avgPoints;
    private Double avg4d1Points;
    private Double stdev;
    private Double isTeam;
    private Double isSprint;
    private Double teamId;
    private Integer daysSinceFirstRace;
    private Integer fp1Pos;
    private Double fp1Gap;
    private Double fp1LapsDone;

    private NSAD() {
    }

    public Row toRegressionRow() {
        return RowFactory.create(actualPoints.doubleValue(), toFeaturesVector());
    }

    public Vector toFeaturesVector() {
        return Vectors.dense(avgPoints, avg4d1Points, stdev, isTeam, isSprint, teamId, daysSinceFirstRace, fp1Pos, fp1Gap, fp1LapsDone);
    }

    public static StructType regressionSchema() {
        Attribute[] attributes = new Attribute[]{
                NumericAttribute.defaultAttr().withName("Average Points"),
                NumericAttribute.defaultAttr().withName("4-Race Average"),
                NumericAttribute.defaultAttr().withName("Standard Deviation"),
                BinaryAttribute.defaultAttr().withName("Is Team"),
                BinaryAttribute.defaultAttr().withName("Is Sprint"),
                NominalAttribute.defaultAttr().withName("Team ID").withNumValues(11),
                NumericAttribute.defaultAttr().withName("Days Since First Race"),
                NominalAttribute.defaultAttr().withName("FP1 Position").withNumValues(24),
                NumericAttribute.defaultAttr().withName("FP1 Gap"),
                NumericAttribute.defaultAttr().withName("FP1 Laps Done")
        };

        StructField featuresField = new AttributeGroup("features", attributes).toStructField();
        return new StructType(new StructField[]{
                DataTypes.createStructField("label", DataTypes.DoubleType, false),
                featuresField
        });
    }

    public static NSAD unlabelled(FullPointEntity fullPointEntity,
                                  String raceName,
                                  boolean isSprint,
                                  double teamId,
                                  int daysSinceFirstRace,
                                  Long fp1Pos,
                                  Double fp1Gap,
                                  Double fp1LapsDone) {
        NSAD result = new NSAD();
        List<Double> pointList = getListOfPoints(fullPointEntity.getRaceList(), raceName);
        result.setAvgPoints(ScoreCalculator.calcAveragePoints(pointList));
        result.setAvg4d1Points(ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList)));
        result.setStdev(MathUtils.stdev(pointList));
        result.setIsTeam(booleanToDouble(fullPointEntity.isTeam()));
        result.setIsSprint(booleanToDouble(isSprint));
        result.setTeamId(teamId);
        result.setDaysSinceFirstRace(daysSinceFirstRace);
        result.setFp1Pos(fp1Pos == null ? 23 : Math.toIntExact(fp1Pos));
        result.setFp1Gap(fp1Gap == null ? -1 : fp1Gap);
        result.setFp1LapsDone(fp1LapsDone == null ? -1 : fp1LapsDone);
        return result;
    }

    public static NSAD full(FullPointEntity fullPointEntity,
                            String raceName,
                            int meetingEntityReference,
                            boolean isSprint,
                            double teamId,
                            int daysSinceFirstRace,
                            Long fp1Pos,
                            Double fp1Gap,
                            Double fp1LapsDone) {
        NSAD result = unlabelled(fullPointEntity,
                raceName,
                isSprint,
                teamId,
                daysSinceFirstRace,
                fp1Pos,
                fp1Gap,
                fp1LapsDone);

        result.setMeetingEntityReference(meetingEntityReference);
        result.setActualPoints(fullPointEntity.getRaceList().stream().filter(r -> r.name().equals(raceName)).findFirst().orElseThrow().totalPoints().intValue());
        return result;
    }

    public static NSAD fromRecord(NonSprintAggregateDataRecord record) {
        NSAD result = new NSAD();
        result.setId(record.getId());
        result.setMeetingEntityReference(record.getMeetingEntityReference());
        result.setActualPoints(record.getActualPoints());
        result.setAvgPoints(record.getAvgPoints());
        result.setAvg4d1Points(record.getAvg_4d1Points());
        result.setStdev(record.getStdev());
        result.setIsTeam(record.getIsTeam());
        result.setIsSprint(record.getIsSprint());
        result.setTeamId(record.getTeamId());
        result.setDaysSinceFirstRace(record.getDaysSinceFirstRace());
        result.setFp1Pos(record.getFp1Pos());
        result.setFp1Gap(record.getFp1Gap());
        result.setFp1LapsDone(record.getFp1LapsDone().doubleValue());
        return result;
    }

    private static List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }

    private static double booleanToDouble(boolean b) {
        return b ? 1d : 0d;
    }


}
