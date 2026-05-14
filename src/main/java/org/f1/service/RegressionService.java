package org.f1.service;

import org.apache.spark.ml.attribute.AttributeGroup;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.f1.controller.model.response.TrainModelResponse;
import org.f1.domain.*;
import org.f1.domain.openf1.SessionResult;
import org.f1.parsing.CSVParsing;
import org.f1.regression.EvaluationResult;
import org.f1.repository.NSADRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.f1.domain.EntityType.DRIVER;
import static org.f1.domain.EntityType.TEAM;

@Service
public class RegressionService {

    private static final Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER, 2024);
    private static final Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM, 2024);
    private static final Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER, 2026);
    private static final Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM, 2026);
    private static final Set<FullPointEntity> drivers2023 = CSVParsing.parseFullPointEntities("Drivers_Full_2023.csv", DRIVER, 2023);
    private static final Set<FullPointEntity> teams2023 = CSVParsing.parseFullPointEntities("Teams_Full_2023.csv", TEAM, 2023);
    private static final Set<FullPointEntity> drivers2025 = CSVParsing.parseFullPointEntities("Drivers_Full_2025.csv", DRIVER, 2025);
    private static final Set<FullPointEntity> teams2025 = CSVParsing.parseFullPointEntities("Teams_Full_2025.csv", TEAM, 2025);


    private final NSADRepository nsadRepository;
    private final MeetingService meetingService;
    private final MERService merService;
    private final SparkSession sparkSession;
    private final SessionResultService sessionResultService;

    public RegressionService(NSADRepository nsadRepository, MeetingService meetingService, MERService merService, SparkSession sparkSession, SessionResultService sessionResultService) {
        this.merService = merService;
        this.nsadRepository = nsadRepository;
        this.meetingService = meetingService;
        this.sparkSession = sparkSession;
        this.sessionResultService = sessionResultService;
    }


    public void populateNSADRegressionData() {
        populateNSADyear(DRIVER_SET, 2026);
        populateNSADyear(drivers2025, 2025);
        populateNSADyear(drivers2024, 2024);
        populateNSADyear(drivers2023, 2023);
        populateNSADyear(TEAM_SET, 2026);
        populateNSADyear(teams2025, 2025);
        populateNSADyear(teams2024, 2024);
        populateNSADyear(teams2023, 2023);

    }

    public TrainModelResponse trainNSADRegressionModel() throws IOException {
        List<NSAD> nsadSet = nsadRepository.getAll();

        Dataset<Row> dataSet = sparkSession.createDataFrame(
                nsadSet.stream()
                        .map(NSAD::toRegressionRow)
                        .collect(Collectors.toList()),
                NSAD.regressionSchema()
        );

        EvaluationResult bestResult = EvaluationResult.parallelGridSearch(dataSet);
        GBTRegressionModel bestModel = bestResult.getTrainedModel();

        String[] featureNames = Arrays.stream(
                AttributeGroup.fromStructField(NSAD
                        .regressionSchema()
                        .fields()[1])
                        .attributes().get())
                .map(attr -> attr.name().get())
                .toArray(String[]::new);

        Map<String, Double> featureImportanceMap = new HashMap<>();
        Vector featureImportances = bestModel.featureImportances();

        for (int i = 0; i < featureNames.length; i++) {
            double importance = featureImportances.apply(i);
            featureImportanceMap.put(featureNames[i], importance);
        }

        Map<String, Double> sortedFeatureImportanceMap = featureImportanceMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        String modelPath = "src/main/resources/regressionModel2";
        bestModel.write().overwrite().save(modelPath);

        return new TrainModelResponse(EvaluationResult.getBestParams(bestModel), bestResult.getMeanSquaredError(), sortedFeatureImportanceMap);
    }


    private void populateNSADyear(Set<FullPointEntity> fullPointEntities, int year) {
        Set<NSAD> returnSet = new HashSet<>();

        for (Meeting meeting : Meeting.getMeetings(year)) {
            String shortName = meeting.getShortName();
            boolean isSprint = meeting.getSprintYears().contains(year);
            int daysSinceFirstRace = meetingService.getDaysSinceFirstRace(year, meeting.getFullNames());

            for (FullPointEntity entity : fullPointEntities) {
                if (entity.getRaceNameList().contains(shortName)) {
                    List<Double> pointList = getListOfPoints(entity.getRaceList(), shortName);
                    if (!pointList.isEmpty()) {
                        MeetingEntityReference mer = merService.getOrCreateMeetingEntityReference(year, meeting, entity);

                        returnSet.add(buildNsad(mer, entity, shortName, daysSinceFirstRace, isSprint));

                    } else {
                        System.out.println("Skipping NSAD entry for entity " + entity.getName() + " with year " + year + " with circuit " + shortName);
                    }
                }
            }
        }
        nsadRepository.saveNSAD(returnSet);
    }

    private NSAD buildNsad(MeetingEntityReference mer, FullPointEntity entity, String shortName, int daysSinceFirstRace, boolean isSprint) {
        List<SessionResult> fp1Results = sessionResultService.getSessionResultsFromName(mer, "Practice 1");
        int size = fp1Results.size();
        Long outPos = null;
        Double outLaps = null;
        Double outGap = null;

        if (!fp1Results.isEmpty() && fp1Results.stream().allMatch(sessionResult -> sessionResult.getPosition() != null && sessionResult.getGapToLeader() != null && sessionResult.getNumberOfLaps() != null)) {
            if (size > 1) {
                double pos = 0;
                double gap = 0;
                double laps = 0;

                for (SessionResult sessionResult : fp1Results) {
                    pos += sessionResult.getPosition();
                    gap += sessionResult.getGapToLeader();
                    laps += sessionResult.getNumberOfLaps();
                }
                outPos = Math.round(pos / size);
                outGap = gap / size;
                outLaps = laps / size;
            } else {
                SessionResult fp1Result = fp1Results.getFirst();
                outPos = Long.valueOf(fp1Result.getPosition());
                outGap = fp1Result.getGapToLeader();
                outLaps = (double) fp1Result.getNumberOfLaps();
            }
        }

        return NSAD.full(entity, shortName, mer.getId(), isSprint, mer.getTeamId(), daysSinceFirstRace, outPos, outGap, outLaps);
    }


    private List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }


}
