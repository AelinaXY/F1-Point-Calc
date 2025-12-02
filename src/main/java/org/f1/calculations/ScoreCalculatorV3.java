package org.f1.calculations;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel;
import org.f1.domain.FullPointEntity;
import org.f1.domain.NSAD;
import org.f1.domain.TeamLookup;
import org.f1.repository.TeamRepository;
import org.f1.service.DriverService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculatorV3 implements ScoreCalculatorInterface {
    private final JavaSparkContext sparkContext;
    private final GradientBoostedTreesModel gradientBoostedTreesModel;
    private final TeamRepository teamRepository;
    private final DriverService driverService;


    public ScoreCalculatorV3(JavaSparkContext javaSparkContext, TeamRepository teamRepository, DriverService driverService) {
        this.sparkContext = javaSparkContext;

        this.gradientBoostedTreesModel = GradientBoostedTreesModel
                .load(sparkContext.sc(), "src/main/resources/regressionModelV3");
        this.teamRepository = teamRepository;
        this.driverService = driverService;
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

        NSAD nsad = NSAD.buildUnlabelledNSAD(fullPointEntity, raceName, isSprint, teamId);

        return gradientBoostedTreesModel.predict(nsad.toLabeledPoint().features());
    }

    private Integer getDriverMerId(FullPointEntity driver) {
        return driverService.getLatestTeam(driver.getName());
    }

    private Integer getTeamId(FullPointEntity team) {
        return teamRepository.getTeam(TeamLookup.csvToPreferred(team.getName()));
    }


}
