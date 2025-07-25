package org.f1;

import org.f1.calculations.RawDataCalculationV2;
import org.f1.calculations.RegressionDataCalculation;
import org.f1.calculations.ScoreCalculator;
import org.f1.calculations.ScoreCalculatorV2;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.stream.Collectors;

import static org.f1.enums.EntityType.*;


public class Main {

    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);
    private static final String RACE_NAME = "Belgium";
    private static final boolean IS_SPRINT = true;

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());

        RawDataCalculationV2 rawDataCalculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 111.9, 3L, RACE_NAME, IS_SPRINT, new ScoreCalculator());
        RegressionDataCalculation regressionDataCalculation = new RegressionDataCalculation(DRIVER_SET, TEAM_SET, 111.9, 3L, RACE_NAME);

        ScoreCard previousScoreCard = rawDataCalculation.createPreviousScoreCard(List.of("Fernando Alonso", "Oliver Bearman", "Gabriel Bortoleto", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"));
        rawDataCalculation.calculate(previousScoreCard);

//        regressionDataCalculation.regressionCalculation();

//        regressionDataCalculation.compareScoreCalculators();
    }

    //TEAMS FLOW VS DRIVERS FLOW

    //VIRTUAL THREADS AND STREAM GATHERERS!!!

//    Test

//    After Spain
//    [0.5900000000000003, 0.3200000000000001, 0.08999999999999958]=262.2117625477216

//    After Canada
//    [0.635, 0.2800000000000001, 0.08499999999999991]=266.8458486748592

//    With Track Similarity Mapping
//    [0.5800000000000003, 0.36000000000000015, 0.059999999999999554]=264.6386895301757

//    After Austria
//    [0.5600000000000003, 0.38000000000000017, 0.059999999999999554]=269.9495371224135

//    With improved regression
//    [0.54, 0.46, 0]=257.85154744896784

//    After Britain
//    Average: 0.49
//    4d1Avg: 0.44
//    Forecast: 0.01
//    Track Similarity: 0.134
//    Sprint Mult: 1.15
//    MSE: 242.4


    //OLD CPU TIME: 9919ms
    //NEW CPU TIME: 410ms


}