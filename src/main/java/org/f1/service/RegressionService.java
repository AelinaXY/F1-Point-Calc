package org.f1.service;

import org.f1.calculations.ScoreCalculator;
import org.f1.domain.*;
import org.f1.parsing.CSVParsing;
import org.f1.repository.MERRepository;
import org.f1.repository.NSADRepository;
import org.f1.repository.TeamRepository;
import org.f1.utils.MathUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.f1.domain.EntityType.DRIVER;
import static org.f1.domain.EntityType.TEAM;

@Service
public class RegressionService {

    private static final Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
    private static final Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM);
    private static final Set<FullPointEntity> DRIVER_SET = CSVParsing.parseFullPointEntities("Drivers_Full.csv", DRIVER);
    private static final Set<FullPointEntity> TEAM_SET = CSVParsing.parseFullPointEntities("Teams_Full.csv", TEAM);


    private final DriverService driverService;
    private final NSADRepository nsadRepository;
    private final MERRepository merRepository;
    private final MeetingService meetingService;
    private final TeamRepository teamRepository;

    public RegressionService(DriverService driverService, NSADRepository nsadRepository, MERRepository merRepository, MeetingService meetingService, TeamRepository teamRepository) {
        this.driverService = driverService;
        this.nsadRepository = nsadRepository;
        this.merRepository = merRepository;
        this.meetingService = meetingService;
        this.teamRepository = teamRepository;
    }


    public void populateNSADRegressionData() {
        populateNSADyear(DRIVER_SET, 2025);
        populateNSADyear(drivers2024, 2024);
        populateNSADyear(TEAM_SET, 2025);
        populateNSADyear(teams2024, 2024);
    }

    private void populateNSADyear(Set<FullPointEntity> fullPointEntities, int year) {
        Set<NSAD> returnSet = new HashSet<>();
        for (Meeting meeting : Meeting.getNonSprintMeetings(year)) {
            String shortName = meeting.getShortName();
            for (FullPointEntity entity : fullPointEntities) {
                if (entity.getRaceNameList().contains(shortName)) {
                    List<Double> pointList = getListOfPoints(entity.getRaceList(), shortName);
                    if (!pointList.isEmpty()) {
                        Integer id;
                        if (entity.isDriver()) {
                            id = getDriverMerId(year, meeting, entity);
                        } else {
                            id = getTeamMerId(year, meeting, entity);
                        }

                        Integer actualPoints = entity.getRaceList().stream().filter(r -> r.name().equals(shortName)).findFirst().orElseThrow().totalPoints().intValue();
                        Double avgPoints = ScoreCalculator.calcAveragePoints(pointList);
                        Double avg4d1Points = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointList));
                        Double stdev = MathUtils.stdev(pointList);

                        NSAD nsad = new NSAD(null, id, actualPoints, avgPoints, avg4d1Points, stdev);
                        returnSet.add(nsad);
                    } else {
                        System.out.println("Skipping NSAD entry for entity " + entity.getName() + " with year " + year + " with circuit " + shortName);
                    }
                }
            }
        }
        nsadRepository.saveNSAD(returnSet);
    }

    private Integer getDriverMerId(int year, Meeting meeting, FullPointEntity driver) {
        DriverMeetingReference driverMeetingReference = driverService.getDriverMRFromYearAndMeetingName(driver.getName(), year, meeting.getFullNames());

        return merRepository.saveMeetingReference(driverMeetingReference);
    }

    private Integer getTeamMerId(int year, Meeting meeting, FullPointEntity team) {
        Integer meetingId = meetingService.getMeeting(year, meeting.getFullNames());
        Integer teamId = teamRepository.getTeam(TeamLookup.csvToPreferred(team.getName()));

        return merRepository.saveMeetingReference(new DriverMeetingReference(null, teamId, meetingId));
    }

    private List<Double> getListOfPoints(List<Race> raceList, String currentRace) {
        return raceList
                .stream()
                .takeWhile(race -> !race.name().equals(currentRace))
                .map(r -> r.qualiPoints() + r.racePoints()).toList();
    }
}
