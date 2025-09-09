package org.f1.modes;

import org.f1.agents.CostCapMultAgent;
import org.f1.calculations.ActualScoreCalculator;
import org.f1.calculations.RawDataCalculationV2;
import org.f1.calculations.ScoreCalculator;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

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
        //List of Races

        //Set all driver and team costs to their base price

        for (FullPointEntity driver : DRIVER_SET) {
            driver.setCost(driver.getBaseCost());
        }
        for (FullPointEntity team : TEAM_SET) {
            team.setCost(team.getBaseCost());
        }
        RawDataCalculationV2 rawDataCalculation = new RawDataCalculationV2(DRIVER_SET, TEAM_SET, 100, 3L, RACE_NAMES_2024.getFirst(), false, new ActualScoreCalculator(), RACE_NAMES_2024.size(), 0);

        ScoreCard previousScoreCard = rawDataCalculation.createPreviousScoreCard(List.of(), List.of(), 0);
        SequencedMap<ScoreCard, DifferenceEntity> outputMap = rawDataCalculation.calculate(previousScoreCard, false);


        for (double i = 0.0d; i <= 2.0d; i += 0.5) {
            agents.add(new CostCapMultAgent(outputMap.firstEntry().getKey(),
                    new LinkedHashMap<>(),
                    100d,
                    0d,
                    i));
        }

        agents.stream().gather(Gatherers.mapConcurrent(100, a -> a)).forEach(agent -> {
            Set<FullPointEntity> agentsDrivers = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
            Set<FullPointEntity> agentsTeams = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", DRIVER);

            ActualScoreCalculator actualScoreCalculator = new ActualScoreCalculator();

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
            }
            System.out.println("agent complete");
        });

        agents.stream().sorted((a,b) -> a.getScore() > b.getScore() ? -1 : 1).forEach(agent -> {
            System.out.println(agent);
        });


        //Loop through every race
        //Inside loop take all the agents and their current team and costcap and work out their best team
        //Replace team with new best team
        //Update costcap and update score with their actual score

        //At the end of the loop go through all drivers and use their actual score to work out their cost change and change the cost to reflect this


        //At the end loop through the agents outputting their score and how much their costcapchangemult was


    }
}
