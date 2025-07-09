package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.Track;
import org.f1.parsing.CSVParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreCalculatorV2 implements ScoreCalculatorInterface {

    private Map<String, Track> TRACK_MAP = CSVParsing.parseTracks("Tracks_Normalised.csv");
    private Double trackSimilarityWeight = 0.06;


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

        Map<String, Double> trackSimilarities = TRACK_MAP.get(raceName).getDistanceToOtherTrack();

        List<Double> qualiPoints = currentRaces.stream().map(r ->
                {
                    String currentRaceName = r.name();
                    double similarity = trackSimilarities.get(currentRaceName);

                    double runningTotal = r.qualiPoints();

                    runningTotal += r.qualiPoints() * similarity * trackSimilarityWeight;
                    return runningTotal;
                }
        ).toList();

        Double predictedQualiPoints = calculatePredictedQuali(qualiPoints, raceName);


        return predictedQualiPoints;
    }

    private Double calculatePredictedQuali(List<Double> racesList, String raceName) {
        double runningTotal = 0;

        for (Double race : racesList) {
            runningTotal += race / racesList.size();
        }

        if (runningTotal < 1) {
            return 0.0;
        }

        if (racesList.size() > 4) {
            double lastFourRunningTotal = 0;

            for (Double race : racesList.subList(racesList.size() - 5, racesList.size())) {
                lastFourRunningTotal += race / 5;
            }

            if (lastFourRunningTotal < 0.61) {
                return 0.0;
            } else if (lastFourRunningTotal > runningTotal * 2.0)
            {
                return lastFourRunningTotal;
            }
            else
            {
                return runningTotal;
            }
        }

        return runningTotal;
    }

    public void setTrackSimilarityWeight(Double trackSimilarityWeight) {
        this.trackSimilarityWeight = trackSimilarityWeight;
    }
}
