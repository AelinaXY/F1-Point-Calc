package org.f1.controller;

import org.f1.calculations.*;
import org.f1.controller.model.request.OptimalTeamRequest;
import org.f1.controller.model.response.OptimalTeamResponse;
import org.f1.controller.model.response.PredictResponse;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.f1.domain.EntityType.DRIVER;
import static org.f1.domain.EntityType.TEAM;

@RestController
@RequestMapping("/api/job")
public class JobController {
    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);
    private static final String DEFAULT_RACE_NAME = "Singapore";
    private static final boolean DEFAULT_IS_SPRINT = false;
    private static final int DEFAULT_RACES_LEFT = 6;
    private static final double DEFAULT_COST_CAP_MULT = 1.2;


    private final RawDataCalculationV2 calculation;
    private final RegressionDataCalculation regressionData;

    private final ScoreCalculatorV3 scoreCalculator;

    public JobController(ScoreCalculatorV3 scoreCalculatorV3) {
        this.scoreCalculator = scoreCalculatorV3;
        List<String> driversNoLongerExistsIn2025 = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExistsIn2025.contains(d.getName())).collect(Collectors.toSet());

        calculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 0, 0, DEFAULT_RACE_NAME, DEFAULT_IS_SPRINT, scoreCalculatorV3, DEFAULT_RACES_LEFT, DEFAULT_COST_CAP_MULT);
        regressionData = new RegressionDataCalculation(DRIVER_SET, TEAM_SET, scoreCalculatorV3);
    }


    @PostMapping("/optimalTeam")
    public ResponseEntity<?> optimalTeam(@RequestBody OptimalTeamRequest optimalTeamRequest,
                                         @RequestParam List<String> driverList,
                                         @RequestParam List<String> teamList,
                                         @RequestParam(defaultValue = "2") int changeLimit) {
        if (driverList.isEmpty() || teamList.isEmpty() || !validDriverList(driverList) || !validTeamList(teamList)) {
            return new ResponseEntity<>("Invalid Driver or Team List. Driver List: %s. Team List: %s.".formatted(driverList, teamList), HttpStatus.BAD_REQUEST);
        }

        populateRawDataCalculation(optimalTeamRequest);
        calculation.resetValues();
        ScoreCard originalScoreCard = calculation.createPreviousScoreCard(driverList, teamList, optimalTeamRequest.costCapMult());
        SequencedMap<ScoreCard, DifferenceEntity> outputMap = calculation.calculate(originalScoreCard, false, 10, changeLimit);

        OptimalTeamResponse response = new OptimalTeamResponse(originalScoreCard.toJSON(), outputMap.sequencedKeySet().stream().map(ScoreCard::toJSON).toList(), outputMap.sequencedValues().stream().map(DifferenceEntity::toJSON).toList());


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

    @GetMapping("/predict")
    public ResponseEntity<?> predictedCostChange(@RequestParam String raceName,
                                                 @RequestParam boolean isSprint) {
        Set<FullPointEntity> workingSet = new HashSet<>();
        workingSet.addAll(DRIVER_SET);
        workingSet.addAll(TEAM_SET);

        Map<String, PredictResponse> returnMap = workingSet.stream()
                .map(entity -> new AbstractMap.SimpleEntry<>(
                        entity.getName(),
                        new PredictResponse(Math.round(scoreCalculator.calculateScore(entity, raceName, isSprint) * 100.0) / 100.0,
                                Math.round(CostCalculator.calculateCostChange(entity,
                                        raceName,
                                        scoreCalculator.calculateScore(entity, raceName, isSprint)) * 100.0) / 100.0,
                                TEAM_SET.contains(entity))))
                .sorted((v1, v2) ->
                        Double.compare(v2.getValue().predictedPoints(), v1.getValue().predictedPoints()))
                .collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        (v1, v2) -> {
                            throw new IllegalStateException();
                        },
                        LinkedHashMap::new));

        return new ResponseEntity<>(returnMap, HttpStatus.OK);
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
