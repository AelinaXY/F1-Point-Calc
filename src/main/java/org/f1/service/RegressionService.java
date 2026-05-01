package org.f1.service;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
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
    private static final Set<FullPointEntity> drivers2025 = CSVParsing.parseFullPointEntities("Drivers_Full_2025.csv", DRIVER);
    private static final Set<FullPointEntity> teams2025 = CSVParsing.parseFullPointEntities("Teams_Full_2025.csv", TEAM);


    private final DriverService driverService;
    private final NSADRepository nsadRepository;
    private final MERRepository merRepository;
    private final MeetingService meetingService;
    private final TeamRepository teamRepository;
    private final SparkSession sparkSession;

    public RegressionService(DriverService driverService, NSADRepository nsadRepository, MERRepository merRepository, MeetingService meetingService, TeamRepository teamRepository, SparkSession sparkSession) {
        this.driverService = driverService;
        this.nsadRepository = nsadRepository;
        this.merRepository = merRepository;
        this.meetingService = meetingService;
        this.teamRepository = teamRepository;
        this.sparkSession = sparkSession;
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
        GBTRegressionModel bestModel = EvaluationResult.buildRegressor(bestResult.getHyperParameters()).fit(dataSet);

        System.out.println("Best parameters found:");
        System.out.println("Parameters: " + bestResult.getHyperParameters());
        System.out.println("MSE: " + bestResult.getMeanSquaredError());
        System.out.println("MAE: " + bestResult.getMeanAbsoluteError());
        System.out.println("R2: " + bestResult.getRSquared());

        String[] featureNames = {"Average Points", "4-Race Average",
                "Standard Deviation", "Is Team", "Is Sprint", "Team ID"};
        System.out.println("\nFeature Importances:");

        Map<String, Double> featureImportanceMap = new HashMap<>();
        Vector featureImportances = bestModel.featureImportances();
        for (int i = 0; i < featureNames.length; i++) {
            double importance = featureImportances.apply(i);
            System.out.printf("%s: %.4f%n", featureNames[i], importance);

            featureImportanceMap.put(featureNames[i], importance);
        }

        String modelPath = "src/main/resources/regressionModel2";
        bestModel.write().overwrite().save(modelPath);

        return new TrainModelResponse(bestResult.getHyperParameters(), bestResult.getMeanSquaredError(), bestResult.getMeanAbsoluteError(), bestResult.getRSquared(), featureImportanceMap);
    }


    private void populateNSADyear(Set<FullPointEntity> fullPointEntities, int year) {
        Set<NSAD> returnSet = new HashSet<>();
        for (Meeting meeting : Meeting.getMeetings(year)) {
            String shortName = meeting.getShortName();
            for (FullPointEntity entity : fullPointEntities) {
                if (entity.getRaceNameList().contains(shortName)) {
                    List<Double> pointList = getListOfPoints(entity.getRaceList(), shortName);
                    if (!pointList.isEmpty()) {
                        MeetingEntityReference mer;
                        if (entity.isDriver()) {
                            mer = getDriverMerId(year, meeting, entity);
                        } else {
                            mer = getTeamMerId(year, meeting, entity);
                        }

                        boolean isSprint = meeting.getSprintYears().contains(year);

                        NSAD nsad = NSAD.buildFullNSAD(entity, shortName, mer.getId(), isSprint, mer.getTeamId());
                        returnSet.add(nsad);
                    } else {
                        System.out.println("Skipping NSAD entry for entity " + entity.getName() + " with year " + year + " with circuit " + shortName);
                    }
                }
            }
        }
        nsadRepository.saveNSAD(returnSet);
    }

    private MeetingEntityReference getDriverMerId(int year, Meeting meeting, FullPointEntity driver) {
        MeetingEntityReference meetingEntityReference = driverService.getDriverMRFromYearAndMeetingName(driver.getName(), year, meeting.getFullNames());

        return merRepository.saveMeetingReference(meetingEntityReference);
    }

    private MeetingEntityReference getTeamMerId(int year, Meeting meeting, FullPointEntity team) {
        Integer meetingId = meetingService.getMeeting(year, meeting.getFullNames());
        Integer teamId = teamRepository.getTeam(TeamLookup.csvToPreferred(team.getName()));

        return merRepository.saveMeetingReference(new MeetingEntityReference(null, null, teamId, meetingId));
    }

    private List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }


}
