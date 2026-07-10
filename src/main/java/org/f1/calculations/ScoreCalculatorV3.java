package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.attribute.Attribute;
import org.apache.spark.ml.attribute.AttributeGroup;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.DecisionTreeRegressionModel;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.ml.tree.CategoricalSplit;
import org.apache.spark.ml.tree.ContinuousSplit;
import org.apache.spark.ml.tree.InternalNode;
import org.apache.spark.ml.tree.Node;
import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.NSAD;
import org.f1.model.prediction.PredictionStepTrace;
import org.f1.model.prediction.PredictionTrace;
import org.f1.model.prediction.PredictionTreeTrace;
import org.f1.service.NSADFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private GBTRegressionModel gradientBoostedTreesModel;
    private final NSADFactory nsadFactory;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, NSADFactory nsadFactory) {
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
        this.nsadFactory = nsadFactory;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        NSAD nsad = nsadFactory.createUnlabelled(fullPointEntity, Meeting.getMeeting(raceName));
        return gradientBoostedTreesModel.predict(nsad.toFeaturesVector()) + nsad.getBaseline();
    }

    public PredictionTrace calculateScoreWithTrace(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        NSAD nsad = nsadFactory.createUnlabelled(fullPointEntity, Meeting.getMeeting(raceName));
        Vector features = nsad.toFeaturesVector();
        String[] featureNames = resolveFeatureNames(features.size());

        double prediction = gradientBoostedTreesModel.predict(features);
        DecisionTreeRegressionModel[] trees = gradientBoostedTreesModel.trees();
        double[] treeWeights = gradientBoostedTreesModel.treeWeights();

        List<Double> treeContributions = new ArrayList<>(trees.length);
        List<Double> runningTotals = new ArrayList<>(trees.length);
        List<Integer> leafIds = new ArrayList<>(trees.length);
        List<PredictionTreeTrace> treeTraces = new ArrayList<>(trees.length);

        double runningPrediction = 0.0;

        for (int i = 0; i < trees.length; i++) {
            TreeTraceComputation treeTraceComputation = computeTreeTrace(
                    trees[i],
                    treeWeights[i],
                    i,
                    runningPrediction,
                    features,
                    featureNames
            );
            runningPrediction = treeTraceComputation.runningTotalAfterTree();
            treeContributions.add(treeTraceComputation.weightedContribution());
            runningTotals.add(treeTraceComputation.runningTotalAfterTree());
            leafIds.add(treeTraceComputation.leafId());
            treeTraces.add(treeTraceComputation.treeTrace());
        }

        return new PredictionTrace(
                prediction,
                nsad.getBaseline(),
                mapFeatureValues(features, featureNames),
                treeContributions,
                runningTotals,
                leafIds,
                treeTraces
        );
    }

    private TreeTraceComputation computeTreeTrace(DecisionTreeRegressionModel tree,
                                                  double treeWeight,
                                                  int treeIndex,
                                                  double runningPrediction,
                                                  Vector features,
                                                  String[] featureNames) {
        double rawTreePrediction = tree.predict(features);
        double weightedContribution = rawTreePrediction * treeWeight;
        double runningTotalAfterTree = runningPrediction + weightedContribution;
        int leafId = (int) tree.predictLeaf(features);
        List<PredictionStepTrace> path = traceTreePath(tree.rootNode(), features, featureNames);

        PredictionTreeTrace treeTrace = new PredictionTreeTrace(
                treeIndex,
                treeWeight,
                rawTreePrediction,
                weightedContribution,
                runningTotalAfterTree,
                leafId,
                path
        );
        return new TreeTraceComputation(weightedContribution, runningTotalAfterTree, leafId, treeTrace);
    }

    public void reloadModel(){
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
    }

    private Map<String, Double> mapFeatureValues(Vector features, String[] featureNames) {
        Map<String, Double> featureMap = new LinkedHashMap<>();
        for (int i = 0; i < features.size(); i++) {
            featureMap.put(featureNames[i], features.apply(i));
        }
        return featureMap;
    }

    private String[] resolveFeatureNames(int size) {
        String[] featureNames = new String[size];
        for (int i = 0; i < size; i++) {
            featureNames[i] = "feature[" + i + "]";
        }

        AttributeGroup attributeGroup = AttributeGroup.fromStructField(NSAD.regressionSchema().fields()[1]);
        if (attributeGroup.attributes().isDefined()) {
            Attribute[] attributes = attributeGroup.attributes().get();
            for (int i = 0; i < Math.min(size, attributes.length); i++) {
                if (attributes[i] != null && attributes[i].name().isDefined()) {
                    featureNames[i] = attributes[i].name().get();
                }
            }
        }
        return featureNames;
    }

    private List<PredictionStepTrace> traceTreePath(Node rootNode, Vector features, String[] featureNames) {
        List<PredictionStepTrace> path = new ArrayList<>();
        Node node = rootNode;
        int depth = 0;

        while (node instanceof InternalNode internalNode) {
            if (internalNode.split() instanceof ContinuousSplit continuousSplit) {
                PredictionStepTrace stepTrace = buildContinuousStepTrace(depth, continuousSplit, features, featureNames);
                path.add(stepTrace);
                boolean wentLeft = stepTrace.wentLeft();
                node = wentLeft ? internalNode.leftChild() : internalNode.rightChild();
            } else if (internalNode.split() instanceof CategoricalSplit categoricalSplit) {
                PredictionStepTrace stepTrace = buildCategoricalStepTrace(depth, categoricalSplit, features, featureNames);
                path.add(stepTrace);
                boolean wentLeft = stepTrace.wentLeft();
                node = wentLeft ? internalNode.leftChild() : internalNode.rightChild();
            } else {
                break;
            }
            depth++;
        }

        return path;
    }

    private PredictionStepTrace buildContinuousStepTrace(int depth, ContinuousSplit split, Vector features, String[] featureNames) {
        int featureIndex = split.featureIndex();
        double featureValue = features.apply(featureIndex);
        double threshold = split.threshold();
        boolean wentLeft = featureValue <= threshold;

        return new PredictionStepTrace(
                depth,
                featureIndex,
                featureNames[featureIndex],
                featureValue,
                "continuous",
                threshold,
                null,
                wentLeft
        );
    }

    private PredictionStepTrace buildCategoricalStepTrace(int depth, CategoricalSplit split, Vector features, String[] featureNames) {
        int featureIndex = split.featureIndex();
        double featureValue = features.apply(featureIndex);
        double[] leftCategoriesArray = split.leftCategories();
        boolean wentLeft = isCategoryOnLeft(featureValue, leftCategoriesArray);

        return new PredictionStepTrace(
                depth,
                featureIndex,
                featureNames[featureIndex],
                featureValue,
                "categorical",
                null,
                Arrays.stream(leftCategoriesArray).boxed().toList(),
                wentLeft
        );
    }

    private boolean isCategoryOnLeft(double featureValue, double[] leftCategoriesArray) {
        for (double category : leftCategoriesArray) {
            if (category == featureValue) {
                return true;
            }
        }
        return false;
    }

    private record TreeTraceComputation(
            Double weightedContribution,
            Double runningTotalAfterTree,
            Integer leafId,
            PredictionTreeTrace treeTrace
    ) {
    }
}
