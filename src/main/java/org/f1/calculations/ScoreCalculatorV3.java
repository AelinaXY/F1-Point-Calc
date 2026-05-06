package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.regression.GBTRegressionModel;
import org.f1.domain.FullPointEntity;
import org.f1.domain.Meeting;
import org.f1.domain.NSAD;
import org.f1.domain.TeamLookup;
import org.f1.repository.TeamRepository;
import org.f1.service.DriverService;
import org.f1.service.MeetingService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final GBTRegressionModel gradientBoostedTreesModel;
    private final TeamRepository teamRepository;
    private final DriverService driverService;
    private final MeetingService meetingService;
    private final int CURRENT_YEAR = 2026;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, TeamRepository teamRepository, DriverService driverService, MeetingService meetingService) {
        this.gradientBoostedTreesModel = GBTRegressionModel.load("src/main/resources/regressionModel2");
        this.teamRepository = teamRepository;
        this.driverService = driverService;
        this.meetingService = meetingService;
    }

    @Override
    @Cacheable("scoreV3")
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {
        Integer teamId;
        if (fullPointEntity.isDriver()) {
            teamId = getDriverMerId(fullPointEntity);
        } else {
            teamId = getTeamId(fullPointEntity);
        }
        int daysSinceFirstRace = meetingService.getDaysSinceFirstRace(CURRENT_YEAR, Meeting.getMeeting(raceName).getFullNames());

        NSAD nsad = NSAD.buildUnlabelledNSAD(fullPointEntity, raceName, isSprint, teamId, daysSinceFirstRace);
        return gradientBoostedTreesModel.predict(nsad.toFeaturesVector());
    }

    private Integer getDriverMerId(FullPointEntity driver) {
        return driverService.getLatestTeam(driver.getName());
    }

    private Integer getTeamId(FullPointEntity team) {
        return teamRepository.getTeam(TeamLookup.csvToPreferred(team.getName()));
    }


}
