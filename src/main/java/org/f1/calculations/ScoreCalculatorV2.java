package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;

import java.util.ArrayList;
import java.util.List;

public class ScoreCalculatorV2 implements ScoreCalculatorInterface {

    public ScoreCalculatorV2() {
    }

    @Override
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        List<Race> currentRaces = new ArrayList<>();

        for (Race race : fullPointEntity.getRaceList()) {
            if (race.name().equals(raceName)) {
                break;
            }
            currentRaces.add(race);
        }

        Double predictedQualiPoints = calculatePredictedQuali(currentRaces, raceName);


        return predictedQualiPoints;
    }

    private Double calculatePredictedQuali(List<Race> racesList, String raceName) {
        double meanPoints = 0;

        for (Race race : racesList) {
            meanPoints += race.qualiPoints() / racesList.size();
        }

        if (meanPoints < 1) {
            return 0.0;
        }

        if (racesList.size() > 4) {
            double lastFourRunningTotal = 0;

            for (Race race : racesList.subList(racesList.size() - 5, racesList.size())) {
                lastFourRunningTotal += race.qualiPoints() / 5;
            }

            if (lastFourRunningTotal < 0.61) {
                return 0.0;
            } else if (lastFourRunningTotal > meanPoints * 2)
            {
                return lastFourRunningTotal;
            }
            else
            {
                return meanPoints;
            }
        }

        return meanPoints;
    }
}
