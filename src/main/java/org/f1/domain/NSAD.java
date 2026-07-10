package org.f1.domain;

import lombok.Data;
import org.apache.spark.ml.attribute.Attribute;
import org.apache.spark.ml.attribute.AttributeGroup;
import org.apache.spark.ml.attribute.BinaryAttribute;
import org.apache.spark.ml.attribute.NominalAttribute;
import org.apache.spark.ml.attribute.NumericAttribute;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


@Data
public class NSAD {
    private static final StructType REGRESSION_SCHEMA = createRegressionSchema();

    //Non-regression information fields
    private Integer id;
    private MeetingEntityReference meetingEntityReference;
    private Double baseline;
    //Regression label
    private Integer actualPoints;
    //Alt Regression label
    private Double residual;
    //Regression features
    private Double avgPoints;
    private Double avg4d1Points;
    private Double isTeam;
    private Double teamId;
    private Integer fp1Pos;
    private Integer fp2Pos;
    private Integer sqPos;
    private Integer fp3Pos;

    private static final double WEIGHT_FACTOR = 0.08;
    private static final double MAX_WEIGHT = 4.0;

    public Row toRegressionRow() {
        return RowFactory.create(residual, toFeaturesVector(), getWeight());
    }

    private double getWeight() {
        double weightValue = 1.0 + (Math.abs(residual) * WEIGHT_FACTOR);
        return Math.min(weightValue, MAX_WEIGHT);
    }

    public Vector toFeaturesVector() {
        return Vectors.dense(
                avgPoints,
                avg4d1Points,
                isTeam,
                teamId,
                fp1Pos,
                fp2Pos,
                sqPos,
                fp3Pos
        );
    }

    public static StructType regressionSchema() {
        return REGRESSION_SCHEMA;
    }

    private static StructType createRegressionSchema() {
        Attribute[] attributes = new Attribute[]{
                NumericAttribute.defaultAttr().withName("Average Points"),
                NumericAttribute.defaultAttr().withName("4-Race Average"),
                BinaryAttribute.defaultAttr().withName("Is Team"),
                NominalAttribute.defaultAttr().withName("Team ID").withNumValues(11),
                NumericAttribute.defaultAttr().withName("FP1 Position"),
                NumericAttribute.defaultAttr().withName("FP2 Position"),
                NumericAttribute.defaultAttr().withName("SQ Position"),
                NumericAttribute.defaultAttr().withName("FP3 Position"),
        };

        StructField featuresField = new AttributeGroup("features", attributes).toStructField();
        return new StructType(new StructField[]{
                DataTypes.createStructField("label", DataTypes.DoubleType, false),
                featuresField,
                DataTypes.createStructField("weight", DataTypes.DoubleType, false),
        });
    }
}
