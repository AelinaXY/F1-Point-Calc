package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.DecisionTreeRegressionModel;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.tree.CategoricalSplit;
import org.apache.spark.ml.tree.ContinuousSplit;
import org.apache.spark.ml.tree.InternalNode;
import org.apache.spark.ml.tree.LeafNode;
import org.apache.spark.ml.tree.Node;
import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.NSAD;
import org.f1.service.NSADFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private static final String MODEL_PATH = "src/main/resources/regressionModel2";
    private static final String[] FEATURE_NAMES = {
            "Average Points",
            "4-Race Average",
            "Standard Deviation",
            "Is Team",
            "Team ID",
            "Days Since First Race",
            "FP1 Position",
            "FP2 Position",
            "SQ Position",
            "FP3 Position",
            "Quali Conversion Delta",
            "Previous Quali Position",
            "4-Race Average Quali Position"
    };

    private GBTRegressionModel gradientBoostedTreesModel;
    private final NSADFactory nsadFactory;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, NSADFactory nsadFactory) {
        this.gradientBoostedTreesModel = GBTRegressionModel.load(MODEL_PATH);
        this.nsadFactory = nsadFactory;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        NSAD nsad = nsadFactory.createUnlabelled(fullPointEntity, Meeting.getMeeting(raceName), isSprint);
        Vector features = nsad.toFeaturesVector();
        double prediction = gradientBoostedTreesModel.predict(features);

        logPredictionBreakdown(fullPointEntity.getName(), raceName, isSprint, features, prediction);

        return prediction;
    }

    @CacheEvict(value = "scoreV3", allEntries = true)
    public void reloadModel() {
        this.gradientBoostedTreesModel = GBTRegressionModel.load(MODEL_PATH);
    }

    private void logPredictionBreakdown(String entityName, String raceName, boolean isSprint, Vector features, double prediction) {
        if (features.size() != gradientBoostedTreesModel.numFeatures()) {
            throw new IllegalStateException(
                    "Feature size mismatch. model=" + gradientBoostedTreesModel.numFeatures() +
                            ", input=" + features.size()
            );
        }

        System.out.printf("Prediction debug for %s (%s, sprint=%s)%n", entityName, raceName, isSprint);

        DecisionTreeRegressionModel[] trees = gradientBoostedTreesModel.trees();
        double[] weights = gradientBoostedTreesModel.treeWeights();
        double[] contributions = new double[trees.length];
        Integer[] indices = new Integer[trees.length];

        double sumContrib = 0.0;
        for (int i = 0; i < trees.length; i++) {
            double treePred = trees[i].predict(features);
            double contribution = treePred * weights[i];
            contributions[i] = contribution;
            indices[i] = i;
            sumContrib += contribution;
        }

        Arrays.sort(indices, (a, b) -> Double.compare(Math.abs(contributions[b]), Math.abs(contributions[a])));

        int topK = Math.min(10, trees.length);
        for (int i = 0; i < topK; i++) {
            int treeIndex = indices[i];
            DecisionTreeRegressionModel tree = trees[treeIndex];
            double treePred = tree.predict(features);
            double weight = weights[treeIndex];
            double contribution = contributions[treeIndex];
            String path = explainTreePath(tree.rootNode(), features);

            System.out.printf(
                    "tree[%d] pred=%.6f weight=%.6f contrib=%.6f path=%s%n",
                    treeIndex, treePred, weight, contribution, path
            );
        }

        System.out.printf("sumContrib=%.6f modelPred=%.6f%n", sumContrib, prediction);
    }

    private String explainTreePath(Node rootNode, Vector features) {
        StringBuilder path = new StringBuilder();
        Node current = rootNode;

        while (current instanceof InternalNode internalNode) {
            boolean goLeft = internalNode.split().shouldGoLeft(features);
            path.append(describeDecision(internalNode, features, goLeft));
            current = goLeft ? internalNode.leftChild() : internalNode.rightChild();
        }

        if (current instanceof LeafNode leafNode) {
            path.append(" -> leaf(").append(String.format("%.6f", leafNode.prediction())).append(")");
        }

        return path.toString();
    }

    private String describeDecision(InternalNode node, Vector features, boolean goLeft) {
        int featureIndex = node.split().featureIndex();
        double featureValue = features.apply(featureIndex);
        String featureName = getFeatureName(featureIndex);
        String branch = goLeft ? "L" : "R";

        if (node.split() instanceof ContinuousSplit split) {
            String operator = goLeft ? "<=" : ">";
            return String.format("[%s %.6f %s %.6f:%s]", featureName, featureValue, operator, split.threshold(), branch);
        }

        if (node.split() instanceof CategoricalSplit split) {
            return String.format(
                    "[%s %.0f inLeft=%s leftCats=%s:%s]",
                    featureName,
                    featureValue,
                    goLeft,
                    Arrays.toString(split.leftCategories()),
                    branch
            );
        }

        return String.format("[%s %.6f split=%s:%s]", featureName, featureValue, node.split().getClass().getSimpleName(), branch);
    }

    private String getFeatureName(int featureIndex) {
        if (featureIndex >= 0 && featureIndex < FEATURE_NAMES.length) {
            return FEATURE_NAMES[featureIndex];
        }
        return "feature[" + featureIndex + "]";
    }
}
