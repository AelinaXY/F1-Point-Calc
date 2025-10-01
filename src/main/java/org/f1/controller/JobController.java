package org.f1.controller;

import org.f1.calculations.CostCalculator;
import org.f1.calculations.RawDataCalculationV2;
import org.f1.calculations.RegressionDataCalculation;
import org.f1.calculations.ScoreCalculator;
import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.domain.request.OptimalTeamRequest;
import org.f1.domain.response.OptimalTeamResponse;
import org.f1.parsing.CSVParsing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.f1.enums.EntityType.DRIVER;
import static org.f1.enums.EntityType.TEAM;

@RestController
@RequestMapping("/api/job")
public class JobController {
    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);
    private static String DEFAULT_RACE_NAME = "Singapore";
    private static boolean DEFAULT_IS_SPRINT = false;
    private static int DEFAULT_RACES_LEFT = 6;
    private static double DEFAULT_COST_CAP_MULT = 1.2;


    private final RawDataCalculationV2 calculation;
    private final RegressionDataCalculation regressionData;

    public JobController() {
        List<String> driversNoLongerExistsIn2025 = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExistsIn2025.contains(d.getName())).collect(Collectors.toSet());

        ScoreCalculator scoreCalculator = new ScoreCalculator();
        calculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 0, 0, DEFAULT_RACE_NAME, DEFAULT_IS_SPRINT, scoreCalculator, DEFAULT_RACES_LEFT, DEFAULT_COST_CAP_MULT);
        regressionData = new RegressionDataCalculation(DRIVER_SET, TEAM_SET);
    }


    @PostMapping("/optimalTeam")
    public ResponseEntity<?> optimalTeam(@RequestBody OptimalTeamRequest optimalTeamRequest,
                                         @RequestParam() List<String> driverList,
                                         @RequestParam List<String> teamList) {
        if (driverList.isEmpty() || teamList.isEmpty() || !validDriverList(driverList) || !validTeamList(teamList)) {
            return new ResponseEntity<>("Invalid Driver or Team List. Driver List: %s. Team List: %s.".formatted(driverList, teamList), HttpStatus.BAD_REQUEST);
        }

        populateRawDataCalculation(optimalTeamRequest);
        calculation.resetValues();
        ScoreCard originalScoreCard = calculation.createPreviousScoreCard(driverList, teamList, optimalTeamRequest.costCapMult());
        SequencedMap<ScoreCard, DifferenceEntity> outputMap = calculation.calculate(originalScoreCard, false, 10);

        OptimalTeamResponse response = new OptimalTeamResponse(outputMap.sequencedKeySet().stream().map(ScoreCard::toJSON).toList(), outputMap.sequencedValues().stream().map(DifferenceEntity::toJSON).toList());


        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/regressionCalc")
    public ResponseEntity<?> regressionCalc(@RequestParam int iterations) {
        return new ResponseEntity<>(regressionData.regressionCalculation(iterations), HttpStatus.OK);
    }

    @GetMapping("/compareScoreCalcs")
    public ResponseEntity<?> compareScoreCalcs() {
        return new ResponseEntity<>(regressionData.compareScoreCalculators(), HttpStatus.OK);
    }

    @GetMapping("/predictedCostChange")
    public ResponseEntity<?> predictedCostChange(@RequestParam String raceName,
                                                 @RequestParam boolean isSprint) {
        ScoreCalculator calc = new ScoreCalculator();

        DRIVER_SET.addAll(TEAM_SET);
        List<FullPointEntity> sortedList = DRIVER_SET.stream().sorted(Comparator.comparing(BasicPointEntity::getName)).toList();

        List<String> returnList = new ArrayList<>();
        for (FullPointEntity entity : sortedList) {
            returnList.add(entity.getName() + ": " +
                    Math.round(CostCalculator.calculateCostChange(entity, raceName,
                            calc.calculateScore(entity, raceName, isSprint)) * 100.0) / 100.0);
        }
        return new ResponseEntity<>(returnList, HttpStatus.OK);
    }

    private boolean validDriverList(List<String> driverList) {
        Set<String> driverNames = DRIVER_SET.stream().map(FullPointEntity::getName).collect(Collectors.toSet());
        return driverNames.containsAll(driverList);
    }

    private boolean validTeamList(List<String> teamList) {
        Set<String> teamNames = TEAM_SET.stream().map(FullPointEntity::getName).collect(Collectors.toSet());
        return teamNames.containsAll(teamList);
    }

    private void populateRawDataCalculation(OptimalTeamRequest optimalTeamRequest) {
        calculation.setCostCap(optimalTeamRequest.costCap());
        calculation.setRaceName(optimalTeamRequest.raceName());
        calculation.setSprint(optimalTeamRequest.isSprint());
        calculation.setRacesLeft(optimalTeamRequest.racesLeft());
        calculation.setCostCapMult(optimalTeamRequest.costCapMult());
    }
}
