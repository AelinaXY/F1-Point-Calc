package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.f1.domain.*;
import org.f1.domain.openf1.SessionResult;
import org.f1.repository.TeamRepository;
import org.f1.service.DriverService;
import org.f1.service.MERService;
import org.f1.service.MeetingService;
import org.f1.service.SessionResultService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final GBTRegressionModel gradientBoostedTreesModel;
    private final TeamRepository teamRepository;
    private final DriverService driverService;
    private final MeetingService meetingService;
    private final MERService merService;
    private final SessionResultService sessionResultService;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, TeamRepository teamRepository, DriverService driverService, MeetingService meetingService, MERService merService, SessionResultService sessionResultService) {
        this.merService = merService;
        this.sessionResultService = sessionResultService;
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
        this.teamRepository = teamRepository;
        this.driverService = driverService;
        this.meetingService = meetingService;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        MeetingEntityReference meetingEntityReference = merService.getOrCreateMeetingEntityReference(fullPointEntity.getYear(), Meeting.getMeeting(raceName), fullPointEntity);

        List<SessionResult> fp1Results = sessionResultService.getSessionResultsFromName(meetingEntityReference, "Practice 1");
        int size = fp1Results.size();
        Long outPos = null;
        Double outLaps = null;
        Double outGap = null;

        if (!fp1Results.isEmpty() && fp1Results.stream().allMatch(sessionResult -> sessionResult.getPosition() != null && sessionResult.getGapToLeader() != null && sessionResult.getNumberOfLaps() != null)) {
            if (size > 1) {
                double pos = 0;
                double gap = 0;
                double laps = 0;

                for (SessionResult sessionResult : fp1Results) {
                    pos += sessionResult.getPosition();
                    gap += sessionResult.getGapToLeader();
                    laps += sessionResult.getNumberOfLaps();
                }
                outPos = Math.round(pos / size);
                outGap = gap / size;
                outLaps = laps / size;
            } else {
                SessionResult fp1Result = fp1Results.getFirst();
                outPos = Long.valueOf(fp1Result.getPosition());
                outGap = fp1Result.getGapToLeader();
                outLaps = (double) fp1Result.getNumberOfLaps();
            }
        }


        int daysSinceFirstRace = meetingService.getDaysSinceFirstRace(fullPointEntity.getYear(), Meeting.getMeeting(raceName).getFullNames());

        NSAD nsad = NSAD.unlabelled(fullPointEntity, raceName, isSprint, meetingEntityReference.getTeamId(), daysSinceFirstRace, outPos, outGap, outLaps);
        return gradientBoostedTreesModel.predict(nsad.toFeaturesVector());
    }
}
