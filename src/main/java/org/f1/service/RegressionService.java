package org.f1.service;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.GradientBoostedTrees;
import org.apache.spark.mllib.tree.configuration.BoostingStrategy;
import org.apache.spark.mllib.tree.model.DecisionTreeModel;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.controller.model.response.TrainModelResponse;
import org.f1.domain.*;
import org.f1.parsing.CSVParsing;
import org.f1.regression.EvaluationResult;
import org.f1.repository.MERRepository;
import org.f1.repository.NSADRepository;
import org.f1.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private static final Set<FullPointEntity> drivers2023 = CSVParsing.parseFullPointEntities("Drivers_Full_2023.csv", DRIVER);
    private static final Set<FullPointEntity> teams2023 = CSVParsing.parseFullPointEntities("Teams_Full_2023.csv", TEAM);

    private final DriverService driverService;
    private final NSADRepository nsadRepository;
    private final MERRepository merRepository;
    private final MeetingService meetingService;
    private final TeamRepository teamRepository;
    private final JavaSparkContext sparkContext;

    public RegressionService(DriverService driverService, NSADRepository nsadRepository, MERRepository merRepository, MeetingService meetingService, TeamRepository teamRepository, JavaSparkContext sparkContext) {
        this.driverService = driverService;
        this.nsadRepository = nsadRepository;
        this.merRepository = merRepository;
        this.meetingService = meetingService;
        this.teamRepository = teamRepository;
        this.sparkContext = sparkContext;
    }


    public void populateNSADRegressionData() {
        populateNSADyear(DRIVER_SET, 2025);
        populateNSADyear(drivers2024, 2024);
        populateNSADyear(drivers2023, 2023);
        populateNSADyear(TEAM_SET, 2025);
        populateNSADyear(teams2024, 2024);
        populateNSADyear(teams2023, 2023);

    }

    public TrainModelResponse trainNSADRegressionModel() throws IOException {
        List<NSAD> nsadSet = nsadRepository.getAll();

        JavaRDD<LabeledPoint> dataSet = sparkContext.parallelize(
                nsadSet.stream()
                        .map(NSAD::toLabeledPoint)
                        .collect(Collectors.toList())
        );

        Map<Integer, Integer> categoricalFeaturesInfo = new HashMap<>();
        categoricalFeaturesInfo.put(3, 2);

        EvaluationResult bestResult = EvaluationResult.parallelGridSearch(dataSet, sparkContext, categoricalFeaturesInfo);

        BoostingStrategy bestStrategy = BoostingStrategy.defaultParams("Regression");
        bestStrategy.setNumIterations(bestResult.getHyperParameters().getNumIterations());
        bestStrategy.setLearningRate(bestResult.getHyperParameters().getLearningRate());
        bestStrategy.treeStrategy().setMaxDepth(bestResult.getHyperParameters().getMaxDepth());
        bestStrategy.treeStrategy().setMinInstancesPerNode(bestResult.getHyperParameters().getMinInstancesPerNode());
        bestStrategy.treeStrategy().setSubsamplingRate(bestResult.getHyperParameters().getSubsamplingRate());
        GradientBoostedTreesModel bestModel = GradientBoostedTrees.train(dataSet, bestStrategy);

        System.out.println("Best parameters found:");
        System.out.println("Parameters: " + bestResult.getHyperParameters());
        System.out.println("MSE: " + bestResult.getMeanSquaredError());
        System.out.println("MAE: " + bestResult.getMeanAbsoluteError());
        System.out.println("R2: " + bestResult.getRSquared());

        String[] featureNames = {"Average Points", "4-Race Average",
                "Standard Deviation", "Is Team"};
        System.out.println("\nFeature Importances:");

        Map<String, Double> featureImportanceMap = new HashMap<>();
        for (int i = 0; i < featureNames.length; i++) {
            double importance = calculateFeatureImportance(bestModel, i);
            System.out.printf("%s: %.4f%n", featureNames[i], importance);

            featureImportanceMap.put(featureNames[i], importance);
        }

        String modelPath = "src/main/resources/regressionModel2";
        FileSystem fs = FileSystem.get(sparkContext.hadoopConfiguration());
        fs.delete(new Path(modelPath), true);
        bestModel.save(sparkContext.sc(), modelPath);

        return new TrainModelResponse(bestResult.getHyperParameters(), bestResult.getMeanSquaredError(), bestResult.getMeanAbsoluteError(), bestResult.getRSquared(), featureImportanceMap);
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
                        Integer mer;
                        if (entity.isDriver()) {
                            mer = getDriverMerId(year, meeting, entity);
                        } else {
                            mer = getTeamMerId(year, meeting, entity);
                        }


                        NSAD nsad = NSAD.buildFullNSAD(entity, shortName, mer);
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


}
