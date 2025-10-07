package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.openf1.Meeting;
import org.f1.repository.CircuitRepository;
import org.f1.repository.CountryRepository;
import org.f1.repository.MeetingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

        meetings.forEach(meeting -> {
            countryRepository.saveCountry(meeting.country());
            circuitRepository.saveCircuit(meeting.circuit());
            meetingRepository.saveMeeting(meeting);
        });

        return meetings;
    }
}
