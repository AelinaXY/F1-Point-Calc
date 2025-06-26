package org.f1;

import org.f1.calculations.RawDataCalculation;
import org.f1.calculations.RawDataCalculationV2;
import org.f1.calculations.RegressionDataCalculation;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.fullParse("Drivers_Full.csv");
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.fullParse("Teams_Full.csv");
    private static final String RACE_NAME = "Austria";

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());

        RawDataCalculationV2 rawDataCalculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 106.5, 3L, RACE_NAME);
        RegressionDataCalculation regressionDataCalculation = new RegressionDataCalculation(DRIVER_SET, TEAM_SET, 106.5, 3L, RACE_NAME);

        ScoreCard previousScoreCard = rawDataCalculation.createPreviousScoreCard(List.of("Franco Colapinto", "Liam Lawson", "Isack Hadjar", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"));
        rawDataCalculation.calculate(previousScoreCard);

//        regressionDataCalculation.regressionCalculation();
    }


    //VIRTUAL THREADS AND STREAM GATHERERS!!!

//    Test

//    After Spain
//    [0.5900000000000003, 0.3200000000000001, 0.08999999999999958]=262.2117625477216

//    After Canada
//    [0.635, 0.2800000000000001, 0.08499999999999991]=266.8458486748592


    //OLD CPU TIME: 9919ms
    //NEW CPU TIME: 410ms


}