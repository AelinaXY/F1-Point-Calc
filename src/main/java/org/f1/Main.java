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

        RawDataCalculations rawDataCalculations = new RawDataCalculations(DRIVER_SET, TEAM_SET, 104.8, 3l);
        RegressionDataCalculations regressionDataCalculations = new RegressionDataCalculations(DRIVER_SET, TEAM_SET, 104.8, 3l);

//        ScoreCard previousScoreCard = rawDataCalculations.createPreviousScoreCard(List.of("Franco Colapinto", "Liam Lawson", "Isack Hadjar", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"));
//        rawDataCalculations.calculate(previousScoreCard);
        regressionDataCalculations.regressionCalculation();
    }

}