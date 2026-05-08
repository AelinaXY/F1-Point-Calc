package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.f1.domain.*;
import org.f1.repository.TeamRepository;
import org.f1.service.DriverService;
import org.f1.service.MERService;
import org.f1.service.MeetingService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final GBTRegressionModel gradientBoostedTreesModel;
    private final TeamRepository teamRepository;
    private final DriverService driverService;
    private final MeetingService meetingService;
    private final MERService merService;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, TeamRepository teamRepository, DriverService driverService, MeetingService meetingService, MERService merService) {
        this.merService = merService;
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
        this.teamRepository = teamRepository;
        this.driverService = driverService;
        this.meetingService = meetingService;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        MeetingEntityReference meetingEntityReference = merService.getOrCreateMeetingEntityReference(fullPointEntity.getYear(), Meeting.getMeeting(raceName), fullPointEntity);

        int daysSinceFirstRace = meetingService.getDaysSinceFirstRace(fullPointEntity.getYear(), Meeting.getMeeting(raceName).getFullNames());

        NSAD nsad = NSAD.unlabelled(fullPointEntity, raceName, isSprint, meetingEntityReference.getTeamId(), daysSinceFirstRace);
        return gradientBoostedTreesModel.predict(nsad.toFeaturesVector());
    }
}
