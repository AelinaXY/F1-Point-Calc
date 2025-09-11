package org.f1;

import org.f1.calculations.*;
import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;
import org.jooq.meta.derby.sys.Sys;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.*;
import java.util.stream.Collectors;

import static org.f1.enums.EntityType.*;

@SpringBootApplication()
@EnableCaching
public class Main {

    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);
    private static final String RACE_NAME = "Azerbaijan";
    private static final boolean IS_SPRINT = false;
    private static final int RACES_LEFT = 7;

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());

        RawDataCalculationV2 rawDataCalculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 119.1, 3L, RACE_NAME, IS_SPRINT, new ScoreCalculator(), RACES_LEFT, 1.2);
        RegressionDataCalculation regressionDataCalculation = new RegressionDataCalculation(DRIVER_SET, TEAM_SET, 119.1, 3L, RACE_NAME);

        ScoreCard previousScoreCard = rawDataCalculation.createPreviousScoreCard(List.of("Isack Hadjar", "Fernando Alonso", "Gabriel Bortoleto", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"), 0.7);
        SequencedMap<ScoreCard, DifferenceEntity> outputMap = rawDataCalculation.calculate(previousScoreCard, true);

        printTeamMap(outputMap);
//        regressionDataCalculation.regressionCalculation();

//        regressionDataCalculation.compareScoreCalculators();


//        ScoreCalculator calc = new ScoreCalculator();
//
//        DRIVER_SET.addAll(TEAM_SET);
//        List<FullPointEntity> sortedList = DRIVER_SET.stream().sorted(Comparator.comparing(BasicPointEntity::getName)).toList();
//        for(FullPointEntity entity: sortedList)
//        {
//            System.out.printf("Expected Change of %s: %s%n", entity.getName(), CostCalculator.calculateCostChange(entity, RACE_NAME, calc.calculateScore(entity, RACE_NAME, IS_SPRINT)));
//        }
    }

    public static void printTeamMap(SequencedMap<ScoreCard, DifferenceEntity> outputMap) {
        System.out.println("ScoreCards:");
        outputMap.sequencedKeySet().forEach(System.out::println);
        System.out.println("Difference Entities:");
        outputMap.sequencedValues().forEach(System.out::println);
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

//    After Belgium
//    Average: 0.48
//    4d1Avg: 0.44
//    Forecast: 0.01
//    Track Similarity: 0.114
//    Sprint Mult: 1.18
//    MSE: 241.3

//    After Hungary
//    Average: 0.47
//    4d1Avg: 0.45
//    Forecast: 0.01
//    Track Similarity: 0.114
//    Sprint Mult: 1.17
//    MSE: 239.9
}