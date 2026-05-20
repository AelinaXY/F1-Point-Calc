package org.f1.domain;

import lombok.Data;
import org.apache.spark.ml.attribute.Attribute;
import org.apache.spark.ml.attribute.AttributeGroup;
import org.apache.spark.ml.attribute.BinaryAttribute;
import org.apache.spark.ml.attribute.NominalAttribute;
import org.apache.spark.ml.attribute.NumericAttribute;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.f1.generated.tables.records.NonSprintAggregateDataRecord;

@Data
public class NSAD {
    private static final StructType REGRESSION_SCHEMA = createRegressionSchema();

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
    private Double fp1Available;
    private Integer fp1Pos;
    private Double fp1Gap;
    private Double fp1LapsDone;

    public Row toRegressionRow() {
        return RowFactory.create(actualPoints.doubleValue(), toFeaturesVector());
    }

    public Vector toFeaturesVector() {
        return Vectors.dense(
                avgPoints,
                avg4d1Points,
                stdev,
                isTeam,
                isSprint,
                teamId,
                daysSinceFirstRace,
                fp1Available,
                fp1Pos,
                fp1Gap,
                fp1LapsDone
        );
    }

    public static StructType regressionSchema() {
        return REGRESSION_SCHEMA;
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
        result.setFp1Available(record.getFp1Available() ? 1.0 : 0.0);
        result.setFp1Pos(record.getFp1Pos());
        result.setFp1Gap(record.getFp1Gap());
        result.setFp1LapsDone(record.getFp1LapsDone().doubleValue());
        return result;
    }

    private static StructType createRegressionSchema() {
        Attribute[] attributes = new Attribute[]{
                NumericAttribute.defaultAttr().withName("Average Points"),
                NumericAttribute.defaultAttr().withName("4-Race Average"),
                NumericAttribute.defaultAttr().withName("Standard Deviation"),
                BinaryAttribute.defaultAttr().withName("Is Team"),
                BinaryAttribute.defaultAttr().withName("Is Sprint"),
                NominalAttribute.defaultAttr().withName("Team ID").withNumValues(11),
                NumericAttribute.defaultAttr().withName("Days Since First Race"),
                BinaryAttribute.defaultAttr().withName("FP1 Available"),
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
}
