package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.MeetingEntityReference;
import org.f1.domain.openf1.Driver;
import org.f1.domain.openf1.Team;
import org.f1.repository.DriverRepository;
import org.f1.repository.TeamRepository;
import org.f1.utils.StreamUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final TeamRepository teamRepository;
    private final OpenF1Dao openF1Dao;

    public DriverService(OpenF1Dao openF1Dao, DriverRepository driverRepository, TeamRepository teamRepository) {
        this.openF1Dao = openF1Dao;
        this.driverRepository = driverRepository;
        this.teamRepository = teamRepository;
    }

    public List<Driver> populateDrivers() {
        List<Driver> drivers = openF1Dao.getAllDrivers();

        Set<String> teamNameList = drivers.stream().map(d -> d.team().getTeamName()).collect(Collectors.toSet());

        Map<String, Integer> teamNameMap = new HashMap<>();
        for (String teamName : teamNameList) {
            Team team = teamRepository.saveTeam(teamName);
            teamNameMap.put(team.getTeamName(), team.getId());
        }

        Set<Driver> driverSet = drivers.stream().filter(StreamUtils.distinctByDualKey(Driver::meetingId, Driver::driverNumber)).collect(Collectors.toSet());

        driverSet.forEach(driver -> {
            driver.team().setId(teamNameMap.get(driver.team().getTeamName()));
            driverRepository.saveDriver(driver);
        });
        return drivers;
    }

    public String getDriverId(int driverNumber, int sessionId) {
        return driverRepository.getDriverIdFromNumberAndSessionId(driverNumber, sessionId);
    }

    public MeetingEntityReference getDriverMRFromYearAndMeetingName(String fullName, int year, List<String> meetingNames) {
        return driverRepository.getDriverMRFromYearAndMeetingNames(fullName, year, meetingNames);
    }

    public Integer getLatestTeam(String fullName)
    {
        return driverRepository.getLatestTeam(fullName);
    }
}
