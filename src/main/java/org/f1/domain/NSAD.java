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

    //Non-regression information fields
    private Integer id;
    private MeetingEntityReference meetingEntityReference;
    //Regression label
    private Integer actualPoints;
    //Regression features
    private Double avgPoints;
    private Double avg4d1Points;
    private Double stdev;
    private Double isTeam;
    private Double teamId;
    private Integer daysSinceFirstRace;
    private Integer fp1Pos;
    private Double fp1Gap;
    private Double fp1LapsDone;
    private Integer fp2Pos;
    private Double fp2Gap;
    private Double fp2LapsDone;
    private Integer sqPos;
    private Double sqGap;
    private Double sqLapsDone;
    private Integer fp3Pos;
    private Double fp3Gap;
    private Double fp3LapsDone;

    public Row toRegressionRow() {
        return RowFactory.create(actualPoints.doubleValue(), toFeaturesVector());
    }

    public Vector toFeaturesVector() {
        return Vectors.dense(
                avgPoints,
                avg4d1Points,
                stdev,
                isTeam,
                teamId,
                daysSinceFirstRace,
                fp1Pos,
                fp1Gap,
                fp1LapsDone,
                fp2Pos,
                fp2Gap,
                fp2LapsDone,
                sqPos,
                sqGap,
                sqLapsDone,
                fp3Pos,
                fp3Gap,
                fp3LapsDone
        );
    }

    public static StructType regressionSchema() {
        return REGRESSION_SCHEMA;
    }

    private static StructType createRegressionSchema() {
        Attribute[] attributes = new Attribute[]{
                NumericAttribute.defaultAttr().withName("Average Points"),
                NumericAttribute.defaultAttr().withName("4-Race Average"),
                NumericAttribute.defaultAttr().withName("Standard Deviation"),
                BinaryAttribute.defaultAttr().withName("Is Team"),
                NominalAttribute.defaultAttr().withName("Team ID").withNumValues(11),
                NumericAttribute.defaultAttr().withName("Days Since First Race"),
                NominalAttribute.defaultAttr().withName("FP1 Position").withNumValues(24),
                NumericAttribute.defaultAttr().withName("FP1 Gap"),
                NumericAttribute.defaultAttr().withName("FP1 Laps Done"),
                NominalAttribute.defaultAttr().withName("FP2 Position").withNumValues(24),
                NumericAttribute.defaultAttr().withName("FP2 Gap"),
                NumericAttribute.defaultAttr().withName("FP2 Laps Done"),
                NominalAttribute.defaultAttr().withName("SQ Position").withNumValues(24),
                NumericAttribute.defaultAttr().withName("SQ Gap"),
                NumericAttribute.defaultAttr().withName("SQ Laps Done"),
                NominalAttribute.defaultAttr().withName("FP3 Position").withNumValues(24),
                NumericAttribute.defaultAttr().withName("FP3 Gap"),
                NumericAttribute.defaultAttr().withName("FP3 Laps Done")
        };

        StructField featuresField = new AttributeGroup("features", attributes).toStructField();
        return new StructType(new StructField[]{
                DataTypes.createStructField("label", DataTypes.DoubleType, false),
                featuresField
        });
    }
}
