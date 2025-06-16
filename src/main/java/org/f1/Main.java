package org.f1;

import org.f1.calculations.RawDataCalculations;
import org.f1.calculations.RegressionDataCalculations;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.fullParse("Drivers_Full.csv");
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.fullParse("Teams_Full.csv");

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());

        RawDataCalculations rawDataCalculations = new RawDataCalculations(DRIVER_SET, TEAM_SET, 106.5, 3l);
        RegressionDataCalculations regressionDataCalculations = new RegressionDataCalculations(DRIVER_SET, TEAM_SET, 106.5, 3l);

        ScoreCard previousScoreCard = rawDataCalculations.createPreviousScoreCard(List.of("Franco Colapinto", "Liam Lawson", "Isack Hadjar", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"));
        rawDataCalculations.calculate(previousScoreCard);

//        regressionDataCalculations.regressionCalculation();
    }


    //VIRTUAL THREADS AND STREAM GATHERERS!!!

//    Test

//    B4 CANADA
//    [0.5900000000000003, 0.3200000000000001, 0.08999999999999958]=262.2117625477216

//    B4 AUSTRIA
//    [0.6300000000000003, 0.2800000000000001, 0.08999999999999958]=266.8510776750517


}