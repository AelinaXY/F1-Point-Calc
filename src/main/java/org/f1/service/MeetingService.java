package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.openf1.Meeting;
import org.f1.repository.CircuitRepository;
import org.f1.repository.CountryRepository;
import org.f1.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MeetingService {

    CircuitRepository circuitRepository;
    OpenF1Dao openF1Dao;
    CountryRepository countryRepository;
    MeetingRepository meetingRepository;

    public MeetingService(OpenF1Dao openF1Dao, CountryRepository countryRepository, CircuitRepository circuitRepository, MeetingRepository meetingRepository) {
        this.openF1Dao = openF1Dao;
        this.countryRepository = countryRepository;
        this.circuitRepository = circuitRepository;
        this.meetingRepository = meetingRepository;
    }

    public List<Meeting> populateMeetings() {
        List<Meeting> meetings = openF1Dao.getAllMeetings();

        List<Integer> sortedCircuitIds = meetings.stream().map(meeting -> meeting.circuit().getId()).distinct().sorted().toList();
        Map<Integer, Integer> circuitIdMap = IntStream.range(0, sortedCircuitIds.size()).boxed().collect(Collectors.toMap(sortedCircuitIds::get, i -> i));


        meetings.forEach(meeting -> {
            meeting.circuit().setId(
                    circuitIdMap.get(
                            meeting.circuit().getId()));

            countryRepository.saveCountry(meeting.country());
            circuitRepository.saveCircuit(meeting.circuit());
            meetingRepository.saveMeeting(meeting);
        });

        return meetings;
    }

    public Integer getMeeting(int year, List<String> fullNames) {
        return meetingRepository.findMeeting(year, fullNames);
    }

    public int getDaysSinceFirstRace(int year, List<String> fullNames) {
        return meetingRepository.getDaysSinceFirstRace(year, fullNames);
    }

    public Integer getCircuitId(Integer meetingId) {
        return circuitRepository.getId(meetingId);
    }
}
