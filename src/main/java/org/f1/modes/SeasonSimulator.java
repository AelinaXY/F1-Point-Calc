package org.f1.modes;

import org.f1.agents.CostCapMultAgent;
import org.f1.calculations.*;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

import static org.f1.enums.EntityType.DRIVER;
import static org.f1.enums.EntityType.TEAM;

public class SeasonSimulator {

    private static final List<String> RACE_NAMES_2024 = List.of("Bahrain", "Saudi Arabia", "Australia", "Japan", "China", "Miami", "Imola", "Monaco", "Canada", "Spain", "Austria", "Britain", "Hungary", "Belgium", "Netherlands", "Italy", "Azerbaijan", "Singapore", "United States", "Mexico", "Brazil", "Las Vegas", "Qatar", "Abu Dhabi");
    private static final Set<String> SPRINTS_2024 = Set.of("China", "Miami", "Austria", "United States", "Brazil", "Qatar");
    private static Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
    private static Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM);

    public static void main(String[] args) {
        List<CostCapMultAgent> agents = new ArrayList<>();

        for (FullPointEntity driver : DRIVER_SET) {
            driver.setCost(driver.getBaseCost());
        }
        for (FullPointEntity team : TEAM_SET) {
            team.setCost(team.getBaseCost());
        }
        RawDataCalculationV2 rawDataCalculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 100, 3L, RACE_NAMES_2024.getFirst(), false, new ScoreCalculator(), RACE_NAMES_2024.size(), 0);

        ScoreCard previousScoreCard = rawDataCalculation.createPreviousScoreCard(List.of(), List.of(), 0);
        SequencedMap<ScoreCard, DifferenceEntity> outputMap = rawDataCalculation.calculate(previousScoreCard, false);


        for (double i = 0.0d; i <= 2.0d; i += 0.4) {
            int j = 0;
            for(ScoreCard scoreCard : outputMap.keySet() ) {
                agents.add(new CostCapMultAgent(scoreCard,
                        new LinkedHashMap<>(),
                        119.1,
                        0d,
                        i));
                j++;
                if(j == 4)
                {
                  break;
                }
            }
        }

        agents.stream().gather(Gatherers.mapConcurrent(100, a -> a)).forEach(agent -> {
            Set<FullPointEntity> agentsDrivers = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
            Set<FullPointEntity> agentsTeams = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", DRIVER);

            for (FullPointEntity driver : agentsDrivers) {
                driver.setCost(driver.getBaseCost());
            }
            for (FullPointEntity team : agentsTeams) {
                team.setCost(team.getBaseCost());
            }

            ActualScoreCalculator actualScoreCalculator = new ActualScoreCalculator();

//            for( int i = 0; i < RACE_NAMES_2024.size()-7; i++ ) {
//                String raceName = RACE_NAMES_2024.get(i);
//                for (FullPointEntity driver : agentsDrivers) {
//                    changeCost(driver, actualScoreCalculator, raceName);
//                }
//                for (FullPointEntity team : agentsTeams) {
//                    changeCost(team, actualScoreCalculator, raceName);
//                }
//            }


            RawDataCalculationV2 scoreCalculator = new RawDataCalculationV2(agentsDrivers, agentsTeams, 100, 3L, "blank", false, new ScoreCalculator(), 24, agent.getCostCapMultiplier());
            for (int i = 0; i < RACE_NAMES_2024.size(); i++) {
                String raceName = RACE_NAMES_2024.get(i);

                scoreCalculator.setRaceName(raceName);
                scoreCalculator.setCostCap(agent.getCostCap());
                scoreCalculator.setSprint(SPRINTS_2024.contains(raceName));
                scoreCalculator.setRacesLeft(RACE_NAMES_2024.size() - i);
                scoreCalculator.setDriverList(agentsDrivers.stream().filter(d -> d.getRaceNameList().contains(raceName)).collect(Collectors.toList()));


                SequencedMap<ScoreCard, DifferenceEntity> currentOutput = scoreCalculator.calculate(agent.getCurrentScoreCard(), false);
                scoreCalculator.resetValues();

                ScoreCard chosenScoreCard = currentOutput.firstEntry().getKey();

                if (i != 0) {
                    agent.addScoreCard(raceName, chosenScoreCard);
                    agent.setCurrentScoreCard(chosenScoreCard);
                } else {
                    agent.addScoreCard(raceName, agent.getCurrentScoreCard());
                }

                agent.getCurrentScoreCard().intialize(raceName, agent.getCostCap(), SPRINTS_2024.contains(raceName), actualScoreCalculator, RACE_NAMES_2024.size() - i, agent.getCostCapMultiplier());

                agent.addScore(agent.getCurrentScoreCard().getScore());
                agent.addCostCap(agent.getCurrentScoreCard().getCostChange());

                for (FullPointEntity driver : agentsDrivers) {
                    changeCost(driver, actualScoreCalculator, raceName);
                }
                for (FullPointEntity team : agentsTeams) {
                    changeCost(team, actualScoreCalculator, raceName);
                }
            }
            System.out.println("agent complete");
        });

        LinkedHashMap<Double, Double> resultMap = agents.stream().collect(Collectors.toMap(CostCapMultAgent::getCostCapMultiplier, a -> a.getScore()/5, Double::sum, LinkedHashMap::new));

        resultMap.sequencedEntrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(a -> System.out.println("CostCapMult:" + a.getKey() + "| Score:" + a.getValue()));
    }

    private static void changeCost(FullPointEntity entity, ActualScoreCalculator actualScoreCalculator, String raceName) {
        Double score = actualScoreCalculator.calculateScore(entity, raceName, SPRINTS_2024.contains(raceName));
        Double costChange = CostCalculator.calculateCostChange(entity, raceName, score);

        entity.setCost(entity.getCost() + costChange);
    }
}
