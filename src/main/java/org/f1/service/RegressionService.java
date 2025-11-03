package org.f1.service;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.GradientBoostedTrees;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.calculations.ScoreCalculator;
import org.f1.domain.*;
import org.f1.parsing.CSVParsing;
import org.f1.repository.MERRepository;
import org.f1.repository.NSADRepository;
import org.f1.repository.TeamRepository;
import org.f1.utils.MathUtils;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

import static org.f1.domain.EntityType.DRIVER;
import static org.f1.domain.EntityType.TEAM;

@Service
public class RegressionService {

    private static final Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
    private static final Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM);
    private static final Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static final Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);


    private final DriverService driverService;
    private final NSADRepository nsadRepository;
    private final MERRepository merRepository;
    private final MeetingService meetingService;
    private final TeamRepository teamRepository;
    private final JavaSparkContext sparkContext;

    public RegressionService(DriverService driverService, NSADRepository nsadRepository, MERRepository merRepository, MeetingService meetingService, TeamRepository teamRepository) {
        this.driverService = driverService;
        this.nsadRepository = nsadRepository;
        this.merRepository = merRepository;
        this.meetingService = meetingService;
        this.teamRepository = teamRepository;
        SparkConf sparkConf = new SparkConf()
                .setAppName("F1PointCalc")
                .setMaster("local")
                .set("spark.ui.enabled", "false")
                .set("spark.driver.host", "localhost");

        this.sparkContext = new JavaSparkContext(sparkConf);
    }


    public void populateNSADRegressionData() {
        populateNSADyear(DRIVER_SET, 2025);
        populateNSADyear(drivers2024, 2024);
        populateNSADyear(TEAM_SET, 2025);
        populateNSADyear(teams2024, 2024);
    }

    public void trainNSADRegressionModel() {
        List<NSAD> nsadSet = nsadRepository.getAll();

        JavaRDD<LabeledPoint> dataSet = sparkContext.parallelize(nsadSet.stream().map(NSAD::toLabeledPoint).collect(Collectors.toList()));

        int numFolds = 5;
        double bestScore = Double.MAX_VALUE;
        GradientBoostedTreesModel bestModel = null;
        Map<String, Double> bestParams = new HashMap<>();
        Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<>();
        categoricalFeaturesInfo.put(3, 2);

        //Hyperparamters
        int[] numIterations = {20, 50};
        int[] maxDepths = {3, 5, 7, 9};
        double[] learningRates = {0.01, 0.03, 0.1, 0.3};

        int noImprovementCount = 0;
        final int EARLY_STOPPING_ROUNDS = 5;
        final double MIN_IMPROVEMENT = 0.001;

        for (int iterations : numIterations) {
            for (int depth : maxDepths) {
                for (double learningRate : learningRates) {
                    double totalMSE = 0.0;
                    double totalMAE = 0.0;
                    double totalR2 = 0.0;

                    for (int fold = 0; fold < numFolds; fold++) {
                        JavaRDD<LabeledPoint>[] splits = dataSet.randomSplit(
                                new double[]{0.8, 0.2}, fold * 1000L + System.currentTimeMillis()

                        );
                        JavaRDD<LabeledPoint> training = splits[0];
                        JavaRDD<LabeledPoint> testing = splits[1];

                        BoostingStrategy boostingStrategy = BoostingStrategy.defaultParams("Regression");
                        boostingStrategy.setNumIterations(iterations);
                        boostingStrategy.setLearningRate(learningRate);
                        boostingStrategy.treeStrategy().setMaxDepth(depth);
                        boostingStrategy.treeStrategy().setCategoricalFeaturesInfo(categoricalFeaturesInfo);
                        boostingStrategy.treeStrategy().setMinInstancesPerNode(1);
                        boostingStrategy.treeStrategy().setMinInfoGain(0.01);

                        GradientBoostedTreesModel model = GradientBoostedTrees.train(
                                training, boostingStrategy
                        );

                        JavaPairRDD<Double, Double> predictionAndLabel = testing.mapToPair(
                                p -> new Tuple2<>(model.predict(p.features()), p.label())
                        );
                        totalMSE += getMeanSquaredError(predictionAndLabel);
                        totalMAE += getMeanAbsoluteError(predictionAndLabel);
                        totalR2 += getRSquared(predictionAndLabel);

                    }
                    double avgMSE = totalMSE / numFolds;
                    double avgMAE = totalMAE / numFolds;
                    double avgR2 = totalR2 / numFolds;

                    System.out.printf(
                            "Iterations: %d, Depth: %d, LR: %.3f, " +
                                    "MSE: %.2f, MAE: %.2f, R²: %.3f%n",
                            iterations, depth, learningRate,
                            avgMSE, avgMAE, avgR2
                    );

                    if (avgMSE < bestScore) {
                        if (bestScore - avgMSE < MIN_IMPROVEMENT) {
                            noImprovementCount++;
                        } else {
                            noImprovementCount = 0;
                        }

                        bestScore = avgMSE;

                        BoostingStrategy bestStrategy = BoostingStrategy.defaultParams("Regression");
                        bestStrategy.setNumIterations(iterations);
                        bestStrategy.setLearningRate(learningRate);
                        bestStrategy.treeStrategy().setMaxDepth(depth);
                        bestStrategy.treeStrategy().setCategoricalFeaturesInfo(categoricalFeaturesInfo);
                        bestStrategy.treeStrategy().setMinInfoGain(0.001);

                        bestModel = GradientBoostedTrees.train(dataSet, bestStrategy);

                        bestParams.put("iterations", (double) iterations);
                        bestParams.put("depth", (double) depth);
                        bestParams.put("learningRate", learningRate);
                    }

                    if (noImprovementCount >= EARLY_STOPPING_ROUNDS) {
                        System.out.printf("Early stopping triggered - no significant improvement for %s rounds", EARLY_STOPPING_ROUNDS);
                        break;
                    }
                }
            }
        }

        System.out.println("Best Mean Squared Error: " + bestScore);
        System.out.println("Best hyperparameters: " + bestParams);
        System.out.println("Best model:\n" + bestModel.toDebugString());

        // Feature importance analysis
        System.out.println("\nFeature Importances:");
        String[] featureNames = {"Average Points", "4-Race Average", "Standard Deviation", "Is Team"};
        for (int i = 0; i < featureNames.length; i++) {
            double importance = calculateFeatureImportance(bestModel, i);
            System.out.printf("%s: %.4f%n", featureNames[i], importance);
        }
    }

    private double calculateFeatureImportance(GradientBoostedTreesModel model, int featureIndex) {
        double totalGain = 0.0;
        double featureGain = 0.0;

        for (DecisionTreeModel tree : model.trees()) {
            String[] lines = tree.toDebugString().split("\n");
            for (int i = 0; i < lines.length - 2; i++) {
                String line = lines[i];
                if (line.contains("If (feature")) {
                    double leftPredict = extractPredictValue(lines[i + 1]);
                    double rightPredict = extractPredictValue(lines[i + 2]);
                    double gain = Math.abs(leftPredict - rightPredict);

                    totalGain += gain;
                    if (line.contains("If (feature " + featureIndex + " ")) {
                        featureGain += gain;
                    }
                }
            }
        }

        return totalGain > 0 ? featureGain / totalGain : 0.0;
    }

    private double extractPredictValue(String line) {
        if (line.contains("Predict: ")) {
            String valueStr = line.substring(line.indexOf("Predict: ") + 9).trim();
            try {
                return Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }


    private void populateNSADyear(Set<FullPointEntity> fullPointEntities, int year) {
        Set<NSAD> returnSet = new HashSet<>();
        for (Meeting meeting : Meeting.getNonSprintMeetings(year)) {
            String shortName = meeting.getShortName();
            for (FullPointEntity entity : fullPointEntities) {
                if (entity.getRaceNameList().contains(shortName)) {
                    List<Double> pointList = getListOfPoints(entity.getRaceList(), shortName);
                    if (!pointList.isEmpty()) {
                        Integer id;
                        double isTeam;

                        if (entity.isDriver()) {
                            id = getDriverMerId(year, meeting, entity);
                            isTeam = 0d;
                        } else {
                            id = getTeamMerId(year, meeting, entity);
                            isTeam = 1d;
                        }

                        Integer actualPoints = entity.getRaceList().stream().filter(r -> r.name().equals(shortName)).findFirst().orElseThrow().totalPoints().intValue();
                        Double avgPoints = ScoreCalculator.calcAveragePoints(pointList);
                        Double avg4d1Points = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList));
                        Double stdev = MathUtils.stdev(pointList);

                        NSAD nsad = new NSAD(null, id, actualPoints, avgPoints, avg4d1Points, stdev, isTeam);
                        returnSet.add(nsad);
                    } else {
                        System.out.println("Skipping NSAD entry for entity " + entity.getName() + " with year " + year + " with circuit " + shortName);
                    }
                }
            }
        }
        nsadRepository.saveNSAD(returnSet);
    }

    private Integer getDriverMerId(int year, Meeting meeting, FullPointEntity driver) {
        DriverMeetingReference driverMeetingReference = driverService.getDriverMRFromYearAndMeetingName(driver.getName(), year, meeting.getFullNames());

        return merRepository.saveMeetingReference(driverMeetingReference);
    }

    private Integer getTeamMerId(int year, Meeting meeting, FullPointEntity team) {
        Integer meetingId = meetingService.getMeeting(year, meeting.getFullNames());
        Integer teamId = teamRepository.getTeam(TeamLookup.csvToPreferred(team.getName()));

        return merRepository.saveMeetingReference(new DriverMeetingReference(null, teamId, meetingId));
    }

    private List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }

    private double getMeanSquaredError(JavaPairRDD<Double, Double> predictionAndLabel) {
        return predictionAndLabel.mapToDouble(pair ->
                        Math.pow(pair._1 - pair._2, 2))
                .mean();
    }

    private double getMeanAbsoluteError(JavaPairRDD<Double, Double> predictionAndLabel) {
        return predictionAndLabel.mapToDouble(pair ->
                Math.abs(pair._1() - pair._2())).mean();
    }

    private double getRSquared(JavaPairRDD<Double, Double> predictionAndLabel) {
        double mean = predictionAndLabel.mapToDouble(Tuple2::_2).mean();
        double totalSS = predictionAndLabel.mapToDouble(pl ->
                        Math.pow(pl._2() - mean, 2))
                .sum();
        double residualSS = predictionAndLabel.mapToDouble(pl ->
                Math.pow(pl._1() - pl._2(), 2)).sum();
        return 1 - (residualSS / totalSS);
    }
}
