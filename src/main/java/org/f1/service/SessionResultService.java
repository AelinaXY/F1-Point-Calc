package org.f1.service;

import org.f1.dao.OpenF1Dao;
import org.f1.domain.MeetingEntityReference;
import org.f1.domain.SessionResultsSummary;
import org.f1.domain.openf1.SessionResult;
import org.f1.repository.SessionResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionResultService {

    private final DriverService driverService;
    private final SessionResultRepository sessionResultRepository;
    private final OpenF1Dao openF1Dao;

    public SessionResultService(OpenF1Dao openF1Dao, SessionResultRepository sessionResultRepository, DriverService driverService) {
        this.openF1Dao = openF1Dao;
        this.sessionResultRepository = sessionResultRepository;
        this.driverService = driverService;
    }

    public List<SessionResult> populateSessionResults() {
        List<SessionResult> sessionResults = openF1Dao.getAllSessionResults();

        sessionResults.forEach(sessionResult ->
        {
            String driverId = driverService.getDriverId(sessionResult.getDriverNumber(), sessionResult.getSessionId());
            sessionResult.setDriverId(driverId);
            sessionResultRepository.saveSessionResult(sessionResult);
        });

        return sessionResults;
    }

    public List<SessionResult> getSessionResultsFromName(MeetingEntityReference meetingEntityReference, String sessionName) {
        return sessionResultRepository.getSessionResults(meetingEntityReference, sessionName);
    }

    public List<SessionResultsSummary> findMappedResultsForDriverOrTeam(MeetingEntityReference meetingEntityReference, List<String> valueSessionNames, List<String> keySessionNames) {
        return sessionResultRepository.findMappedToQualiResultsForDriverOrTeam(meetingEntityReference, valueSessionNames, keySessionNames);
    }

    public Double getPreviousSessionAvgPos(MeetingEntityReference meetingEntityReference, int numberOfMeetings, String sessionName) {
        return sessionResultRepository.getPreviousSessionAvgPos(meetingEntityReference, numberOfMeetings, sessionName);
    }
}
